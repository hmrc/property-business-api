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

package v6.createAmendForeignPropertyAnnualSubmission

import common.models.errors.*
import shared.controllers.EndpointLogContext
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.{
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData
}
import v6.createAmendForeignPropertyAnnualSubmission.model.request.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino             = Nino("AA123456A")
  private val businessId       = BusinessId("XAIS12345678910")
  private val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private implicit val correlationId: String = "X-123"

  "CreateAmendForeignPropertyAnnualSubmissionService" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendForeignPropertyAnnualSubmissionConnector
          .amendForeignProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAmendForeignPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "service call unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendForeignPropertyAnnualSubmissionConnector
              .amendForeignProperty(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createAmendForeignPropertyAnnualSubmission(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
          "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
          "INVALID_TAX_YEAR"            -> TaxYearFormatError,
          "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
          "INCOME_SOURCE_NOT_FOUND"     -> NotFoundError,
          "INVALID_PAYLOAD"             -> InternalError,
          "INVALID_CORRELATIONID"       -> InternalError,
          "DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
          "SERVER_ERROR"                -> InternalError,
          "SERVICE_UNAVAILABLE"         -> InternalError,
          "OUTSIDE_AMENDMENT_WINDOW"    -> RuleOutsideAmendmentWindowError
        )

        val extraTysErrors = List(
          "MISSING_EXPENSES"         -> InternalError,
          "FIELD_CONFLICT"           -> RulePropertyIncomeAllowanceError,
          "INVALID_INCOME_SOURCE_ID" -> BusinessIdFormatError,
          "PROPERTY_ID_DO_NOT_MATCH" -> RulePropertyIdMismatchError,
          "INVALID_CORRELATION_ID"   -> InternalError,
          "MISSING_ALLOWANCES"       -> InternalError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError).tupled(args))
      }
    }
  }

  trait Test extends MockCreateAmendForeignPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateAmendForeignPropertyAnnualSubmissionService(
      connector = mockAmendForeignPropertyAnnualSubmissionConnector
    )

    private val body: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData(nino, businessId, taxYear, body)

  }

}
