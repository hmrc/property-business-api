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

package v4.createUkPropertyPeriodSummary

import common.models.errors.{
  RuleDuplicateSubmissionError,
  RuleMisalignedPeriodError,
  RuleNotContiguousPeriodError,
  RuleOverlappingPeriodError,
  RuleToDateBeforeFromDateError,
  RuleTypeOfBusinessIncorrectError
}
import shared.controllers.EndpointLogContext
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v4.createUkPropertyPeriodSummary.model.request.{Def1_CreateUkPropertyPeriodSummaryRequestBody, Def1_CreateUkPropertyPeriodSummaryRequestData}
import v4.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryServiceSpec extends ServiceSpec with MockCreateUkPropertyPeriodSummaryConnector {

  implicit override val correlationId: String          = "X-123"
  implicit override val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

  private val nino       = Nino("AA123456A")
  private val taxYear    = TaxYear.fromMtd("2020-21")
  private val businessId = BusinessId("XAIS12345678910")

  private val requestBody =
    Def1_CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

  private val requestData: Def1_CreateUkPropertyPeriodSummaryRequestData =
    Def1_CreateUkPropertyPeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

  private val responseData: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val service = new CreateUkPropertyPeriodSummaryService(
    connector = mockCreateUkPropertyPeriodSummaryConnector
  )

  "service" when {
    "service call successful" should {
      "return mapped result" in {
        MockedCreateUkPropertyPeriodSummaryConnector
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        val result = await(service.createUkProperty(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in {

            MockedCreateUkPropertyPeriodSummaryConnector
              .createUkProperty(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result = await(service.createUkProperty(requestData))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
          "INVALID_PAYLOAD"           -> InternalError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
          "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmissionError,
          "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
          "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
          "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
          "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError,
          "MISSING_EXPENSES"          -> InternalError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors = List(
          "INVALID_INCOMESOURCE_ID"            -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"             -> InternalError,
          "PERIOD_NOT_ALIGNED"                 -> RuleMisalignedPeriodError,
          "PERIOD_OVERLAPS"                    -> RuleOverlappingPeriodError,
          "SUBMISSION_DATE_ISSUE"              -> RuleMisalignedPeriodError,
          "BUSINESS_INCOME_PERIOD_RESTRICTION" -> InternalError
        )

        (errors ++ extraTysErrors).foreach((serviceError _).tupled)
      }
    }
  }

}
