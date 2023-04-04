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

package v2.services

import support.UnitSpec
import v2.mocks.connectors.MockRetrieveForeignPropertyAnnualSubmissionConnector
import api.models.errors._
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult}
import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"
  private val taxYear: TaxYear   = TaxYear.fromMtd("2020-21")

  implicit private val correlationId: String = "X-123"

  trait Test extends MockRetrieveForeignPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveForeignPropertyAnnualSubmissionService(
      connector = mockRetrieveForeignPropertyConnector
    )

    protected val requestData: RetrieveForeignPropertyAnnualSubmissionRequest =
      RetrieveForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear)

    protected val response: RetrieveForeignPropertyAnnualSubmissionResponse =
      RetrieveForeignPropertyAnnualSubmissionResponse("2020-07-07T10:59:47.544Z", None, None)

  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
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

  "unsuccessful" should {
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
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
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

}
