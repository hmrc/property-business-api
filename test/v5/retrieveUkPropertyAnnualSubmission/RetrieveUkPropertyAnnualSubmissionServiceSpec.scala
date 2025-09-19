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

package v5.retrieveUkPropertyAnnualSubmission

import common.models.errors.RuleTypeOfBusinessIncorrectError
import shared.controllers.EndpointLogContext
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v5.retrieveUkPropertyAnnualSubmission.model.{NonUkResult, UkResult}
import v5.retrieveUkPropertyAnnualSubmission.def1.model.request.Def1_RetrieveUkPropertyAnnualSubmissionRequestData
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")
  private val businessId                     = BusinessId("XAIS12345678910")
  private val taxYear                        = TaxYear.fromMtd("2020-21")

  "service" when {
    "a uk result is found" should {
      "return a success result" in new Test {
        MockRetrieveUkPropertyConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, UkResult(response))))

        await(service.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "a non-uk result is found" should {
      "return a RULE_TYPE_OF_BUSINESS_INCORRECT error" in new Test {
        MockRetrieveUkPropertyConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, NonUkResult)))

        await(service.retrieveUkProperty(request)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveUkPropertyConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieveUkProperty(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
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

        (errors ++ extraTysErrors).foreach(args => (serviceError).tupled(args))
      }
    }
  }

  trait Test extends MockRetrieveUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveUkPropertyAnnualSubmissionService(
      connector = mockRetrieveUkPropertyConnector
    )

    protected val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp("2022-06-17T10:53:38Z"), None, None)

    protected val request: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)

  }

}
