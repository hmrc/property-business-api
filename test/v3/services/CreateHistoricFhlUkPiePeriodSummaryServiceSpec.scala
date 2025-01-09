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

import common.models.domain.PeriodId
import common.models.errors._
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, _}
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import uk.gov.hmrc.http.HeaderCarrier
import v3.connectors.MockCreateHistoricFhlUkPiePeriodSummaryConnector
import v3.models.request.common.ukFhlPieProperty.{UkFhlPieExpenses, UkFhlPieIncome}
import v3.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v3.models.request.createHistoricFhlUkPiePeriodSummary.{
  CreateHistoricFhlUkPiePeriodSummaryRequestBody,
  CreateHistoricFhlUkPiePeriodSummaryRequestData
}
import v3.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryResponse

import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryServiceSpec extends ServiceSpec {

  private val nino     = "WE123567A"
  private val fromDate = "2021-01-06"
  private val toDate   = "2021-02-06"
  private val periodId = "2021-01-06_2021-02-06"

  implicit override val correlationId: String = "some-correlation-id"

  "service" when {
    "service call successful" should {
      "return mapped result for regular period summary" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryConnector
          .createPropertyPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse] = await(service.createPeriodSummary(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))
      }

      "return mapped result for consolidated expenses period summary" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryConnector
          .createPropertyPeriodSummary(consolidatedRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse] = await(service.createPeriodSummary(consolidatedRequestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))

      }
    }

    "service call unsuccessful map" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s" return a$downstreamErrorCode from the service" in new Test {
          MockCreateHistoricFhlUkPiePeriodSummaryConnector
            .createPropertyPeriodSummary(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.createPeriodSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO"            -> NinoFormatError,
        "INVALID_TYPE"            -> InternalError,
        "INVALID_PAYLOAD"         -> InternalError,
        "INVALID_CORRELATIONID"   -> InternalError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "DUPLICATE_SUBMISSION"    -> RuleDuplicateSubmissionError,
        "NOT_ALIGN_PERIOD"        -> RuleMisalignedPeriodError,
        "OVERLAPS_IN_PERIOD"      -> RuleOverlappingPeriodError,
        "NOT_CONTIGUOUS_PERIOD"   -> RuleNotContiguousPeriodError,
        "INVALID_PERIOD"          -> RuleToDateBeforeFromDateError,
        "BOTH_EXPENSES_SUPPLIED"  -> RuleBothExpensesSuppliedError,
        "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
        "SERVER_ERROR"            -> InternalError,
        "SERVICE_UNAVAILABLE"     -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockCreateHistoricFhlUkPiePeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateHistoricFhlUkPiePeriodSummaryService(
      connector = mockCreateHistoricFhlUkPiePropertyConnector
    )

    private val income: UkFhlPieIncome = UkFhlPieIncome(Some(129.10), Some(129.11), Some(UkPropertyIncomeRentARoom(Some(144.23))))

    private val expenses: UkFhlPieExpenses = UkFhlPieExpenses(
      Some(3123.21),
      Some(928.42),
      Some(842.99),
      Some(8831.12),
      Some(484.12),
      Some(992.82),
      Some(999.99),
      None,
      Some(UkPropertyExpensesRentARoom(Some(8842.43)))
    )

    private val consolidatedExpenses: UkFhlPieExpenses = UkFhlPieExpenses(None, None, None, None, None, None, None, Some(22.50), None)

    protected val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(expenses))

    protected val consolidatedBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(consolidatedExpenses))

    protected val requestData: CreateHistoricFhlUkPiePeriodSummaryRequestData =
      CreateHistoricFhlUkPiePeriodSummaryRequestData(Nino(nino), requestBody)

    protected val consolidatedRequestData: CreateHistoricFhlUkPiePeriodSummaryRequestData =
      CreateHistoricFhlUkPiePeriodSummaryRequestData(Nino(nino), consolidatedBody)

    protected val responseData: CreateHistoricFhlUkPiePeriodSummaryResponse =
      CreateHistoricFhlUkPiePeriodSummaryResponse(PeriodId(periodId))

  }

}
