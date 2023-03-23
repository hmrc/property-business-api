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
import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockAmendUkPropertyAnnualSubmissionConnector
import v2.models.domain.TaxYear; import api.models.domain.Nino
import api.models.errors._
import api.models.ResponseWrapper
import v2.models.request.amendUkPropertyAnnualSubmission.{ AmendUkPropertyAnnualSubmissionRequest, AmendUkPropertyAnnualSubmissionRequestBody }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "X-123"

  val body: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(None, None)

  private val request = AmendUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendUkPropertyAnnualSubmissionConnector
          .amendUkProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendUkPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendUkPropertyAnnualSubmissionConnector
              .amendUkProperty(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendUkPropertyAnnualSubmission(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
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

        val extraTysErrors = Map(
          "MISSING_EXPENSES" -> InternalError,
          "FIELD_CONFLICT"   -> RulePropertyIncomeAllowanceError,
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockAmendUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendUkPropertyAnnualSubmissionService(
      connector = mockAmendUkPropertyAnnualSubmissionConnector
    )
  }
}
