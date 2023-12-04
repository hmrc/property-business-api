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

package v3.services

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import mocks.MockFeatureSwitches
import uk.gov.hmrc.http.HeaderCarrier
import v3.connectors.MockCreateUkPropertyPeriodSummaryConnector
import v3.models.request.createUkPropertyPeriodSummary._
import v3.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryServiceSpec extends ServiceSpec with MockFeatureSwitches {

  implicit private val correlationId: String = "X-123"

  private val nino       = Nino("AA123456A")
  private val taxYear    = TaxYear.fromMtd("2020-21")
  private val businessId = BusinessId("XAIS12345678910")

  private val requestBody =
    CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

  protected val request: CreateUkPropertyPeriodSummaryRequestData =
    CreateUkPropertyPeriodSummaryRequestData(nino, taxYear, businessId, requestBody)

  protected val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test with WIS008Enabled {
        MockCreateUkPropertyConnector
          .createUkProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val result: ServiceOutcome[CreateUkPropertyPeriodSummaryResponse] = await(service.createUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(wis008Enabled: Boolean)(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the servic eand WIS008 is $wis008Enabled" in new Test {

            withWIS008Enabled(wis008Enabled)

            MockCreateUkPropertyConnector
              .createUkProperty(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[CreateUkPropertyPeriodSummaryResponse] = await(service.createUkProperty(request))
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
          "BUSINESS_INCOME_PERIOD_RESTRICTION" -> InternalError
        )

        val wis008Errors = List(
          ("INVALID_SUBMISSION_PERIOD", RuleInvalidSubmissionPeriodError),
          ("INVALID_SUBMISSION_END_DATE", RuleInvalidSubmissionEndDateError)
        )

        (errors ++ extraTysErrors ++ wis008Errors).foreach((serviceError(wis008Enabled = true) _).tupled)

        List(
          "INVALID_SUBMISSION_PERIOD",
          "INVALID_SUBMISSION_END_DATE"
        ).foreach(serviceError(wis008Enabled = false)(_, InternalError))
      }
    }
  }

  private trait Test extends MockCreateUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier    = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateUkPropertyPeriodSummaryService(mockCreateUkPropertyConnector)
  }

  private def withWIS008Enabled(enabled: Boolean): Unit = MockFeatureSwitches.isWIS008Enabled.returns(enabled)

  private trait WIS008Enabled {
    withWIS008Enabled(true)
  }

}
