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

import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockAmendUkPropertyPeriodSummaryConnector
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyPeriodSummary.{ AmendUkPropertyPeriodSummaryRequest, AmendUkPropertyPeriodSummaryRequestBody }

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  val nino: String                   = "AA123456A"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  val businessId: String             = "XAIS12345678910"
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val requestBody: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(None, None)

  private val request: AmendUkPropertyPeriodSummaryRequest = AmendUkPropertyPeriodSummaryRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    businessId = businessId,
    submissionId = submissionId,
    body = requestBody
  )

  trait Test extends MockAmendUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendUkPropertyPeriodSummaryService(
      connector = mockAmendUkPropertyPeriodSummaryConnector
    )
  }

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

        val errors = Seq(
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

        val extraTysErrors = Seq(
          "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"       -> InternalError,
          "INCOME_SOURCE_NOT_COMPATIBLE" -> RuleTypeOfBusinessIncorrectError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
