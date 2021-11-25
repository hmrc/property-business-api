/*
 * Copyright 2021 HM Revenue & Customs
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

import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector.{NonUkResult, UkResult}
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveUkPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryServiceSpec extends UnitSpec with ResponseModelsFixture {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2019-20"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"


  private val requestData = RetrieveUkPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, submissionId)

  trait Test extends MockRetrieveUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveUkPropertyPeriodSummaryService(
      connector = mockConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveUkPropertyConnector.retrieveUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, UkResult(fullResponseModel)))))

        await(service.retrieveUkProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponseModel))
      }
    }

    "a non-uk result is found" should {
      "return a RULE_TYPE_OF_BUSINESS_INCORRECT error" in new Test {
        MockRetrieveUkPropertyConnector
          .retrieveUkProperty(requestData) returns Future.successful(Right(ResponseWrapper(correlationId, NonUkResult)))

        await(service.retrieveUkProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
      }
    }

  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockRetrieveUkPropertyConnector.retrieveUkProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.retrieveUkProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "NO_DATA_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError,
        "INVALID_CORRELATIONID" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}