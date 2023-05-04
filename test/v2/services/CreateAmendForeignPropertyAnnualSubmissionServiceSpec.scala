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

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.connectors.MockCreateAmendForeignPropertyAnnualSubmissionConnector
import v2.models.request.createAmendForeignPropertyAnnualSubmission._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"
  private val taxYear: TaxYear   = TaxYear.fromMtd("2020-21")

  implicit private val correlationId: String = "X-123"

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockAmendForeignPropertyAnnualSubmissionConnector
          .amendForeignProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAmendForeignPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
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
        //"BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
        "INCOME_SOURCE_NOT_FOUND"     -> NotFoundError,
        "MISSING_ALLOWANCES"          -> InternalError,
        "INVALID_PAYLOAD"             -> InternalError,
        "INVALID_CORRELATIONID"       -> InternalError,
        //"DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
        "SERVER_ERROR"                -> InternalError,
        "SERVICE_UNAVAILABLE"         -> InternalError
      )

      val extraTysErrors = List(
        "MISSING_EXPENSES" -> InternalError,
        //"FIELD_CONFLICT"   -> RulePropertyIncomeAllowanceError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockCreateAmendForeignPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateAmendForeignPropertyAnnualSubmissionService(
      connector = mockAmendForeignPropertyAnnualSubmissionConnector
    )

    private val body: CreateAmendForeignPropertyAnnualSubmissionRequestBody = CreateAmendForeignPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequest =
      CreateAmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

  }

}
