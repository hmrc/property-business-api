/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v4.retrieveForeignPropertyPeriodSummary

import api.controllers.EndpointLogContext
import api.models.domain._
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult}
import v4.retrieveForeignPropertyPeriodSummary.model.request.{Def1_RetrieveForeignPropertyPeriodSummaryRequestData, RetrieveForeignPropertyPeriodSummaryRequestData}
import v4.retrieveForeignPropertyPeriodSummary.model.response.{Def1_RetrieveForeignPropertyPeriodSummaryResponse, RetrieveForeignPropertyPeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {

  private val nino         = Nino("AA123456A")
  private val businessId   = BusinessId("XAIS12345678910")
  private val taxYear      = TaxYear.fromMtd("2020-21")
  private val submissionId = SubmissionId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  implicit private val correlationId: String = "X-123"

  "The service" should {
    "map the result" when {
      "the downstream response is successful" in new Test {
        MockRetrieveForeignPropertyConnector
          .retrieveForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ForeignResult(response)))))

        await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "a non-foreign result is found" should {
    "return a RULE_TYPE_OF_BUSINESS_INCORRECT error" in new Test {
      MockRetrieveForeignPropertyConnector
        .retrieveForeignProperty(requestData) returns Future.successful(Right(ResponseWrapper(correlationId, NonForeignResult)))

      await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
    }
  }

  "The service" should {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignPropertyConnector
            .retrieveForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> InternalError,
        "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
        "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = List(
        "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
        "INVALID_CORRELATION_ID"  -> InternalError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveForeignPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveForeignPropertyPeriodSummaryService(
      connector = mockRetrieveForeignPropertyConnector
    )

    protected val response: RetrieveForeignPropertyPeriodSummaryResponse =
      Def1_RetrieveForeignPropertyPeriodSummaryResponse(Timestamp("2020-06-17T10:53:38Z"), "2019-01-29", "2020-03-29", None, None)

    protected val requestData: RetrieveForeignPropertyPeriodSummaryRequestData =
      Def1_RetrieveForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, submissionId)

  }

}
