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

package v4.amendUkPropertyPeriodSummary

import common.models.domain.SubmissionId
import common.models.errors.{RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import shared.controllers.EndpointLogContext
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.amendUkPropertyPeriodSummary.model.request.*

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  private val nino: String         = "AA123456A"
  private val taxYear: TaxYear     = TaxYear.fromMtd("2020-21")
  private val businessId: String   = "XAIS12345678910"
  private val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  implicit override val correlationId: String = "X-123"

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendUkPropertyPeriodSummaryConnector
          .amendUkPropertyPeriodSummary(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendUkPropertyPeriodSummary(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, expectError: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendUkPropertyPeriodSummaryConnector
              .amendUkPropertyPeriodSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendUkPropertyPeriodSummary(request)) shouldBe Left(ErrorWrapper(correlationId, expectError))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
          "INVALID_TAX_YEAR"            -> TaxYearFormatError,
          "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
          "INVALID_SUBMISSION_ID"       -> SubmissionIdFormatError,
          "INVALID_PAYLOAD"             -> InternalError,
          "INVALID_CORRELATIONID"       -> InternalError,
          "NO_DATA_FOUND"               -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> InternalError,
          "DUPLICATE_COUNTRY_CODE"      -> InternalError,
          "MISSING_EXPENSES"            -> InternalError,
          "SERVER_ERROR"                -> InternalError,
          "SERVICE_UNAVAILABLE"         -> InternalError
        )

        val extraTysErrors = List(
          "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"       -> InternalError,
          "INCOME_SOURCE_NOT_COMPATIBLE" -> RuleTypeOfBusinessIncorrectError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError).tupled(args))
      }
    }
  }

  trait Test extends MockAmendUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new AmendUkPropertyPeriodSummaryService(
      connector = mockAmendUkPropertyPeriodSummaryConnector
    )

    private val requestBody: Def1_AmendUkPropertyPeriodSummaryRequestBody = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, None)

    protected val request: Def1_AmendUkPropertyPeriodSummaryRequestData =
      Def1_AmendUkPropertyPeriodSummaryRequestData(Nino(nino), taxYear, BusinessId(businessId), SubmissionId(submissionId), requestBody)

  }

}
