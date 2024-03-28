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

package v4.controllers.createAmendUkPropertyAnnualSubmission

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.controllers.createAmendUkPropertyAnnualSubmission.model.request._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2020-21")

  implicit private val correlationId: String = "X-123"

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockCreateAmendUkPropertyAnnualSubmissionConnector
          .createAmendUkProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAmendUkPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockCreateAmendUkPropertyAnnualSubmissionConnector
              .createAmendUkProperty(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createAmendUkPropertyAnnualSubmission(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
          "INVALID_TAX_YEAR"            -> TaxYearFormatError,
          "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
          "INVALID_PAYLOAD"             -> InternalError,
          "INVALID_CORRELATIONID"       -> InternalError,
          "INCOME_SOURCE_NOT_FOUND"     -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
          "MISSING_ALLOWANCES"          -> InternalError,
          "DUPLICATE_COUNTRY_CODE"      -> InternalError,
          "SERVER_ERROR"                -> InternalError,
          "SERVICE_UNAVAILABLE"         -> InternalError
        )

        val extraTysErrors = List(
          "MISSING_EXPENSES" -> InternalError,
          "FIELD_CONFLICT"   -> RulePropertyIncomeAllowanceError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockCreateAmendUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateAmendUkPropertyAnnualSubmissionService(
      connector = mockCreateAmendUkPropertyAnnualSubmissionConnector
    )

    private val body: Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: Def1_CreateAmendUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(nino, businessId, taxYear, body)

  }

}
