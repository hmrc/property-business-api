/*
 * Copyright 2022 HM Revenue & Customs
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
import v2.models.outcomes.ResponseWrapper
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateHistoricFhlUkPiePeriodSummaryConnector
import v2.models.request.common.ukFhlPieProperty.{UkFhlPieExpenses, UkFhlPieIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryRequest, CreateHistoricFhlUkPiePeriodSummaryRequestBody}
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryResponse

import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryServiceSpec extends ServiceSpec {
  val transactionReference: String = "some-transaction-reference"
  val nino: String = "WE12356753A"
  val fromDate: String = "2021-01-06"
  val toDate: String = "2021-02-06"
  val income: UkFhlPieIncome = UkFhlPieIncome(Some(129.10), Some(129.11),
    Some(UkPropertyIncomeRentARoom(Some(144.23))))
  val expenses: UkFhlPieExpenses = UkFhlPieExpenses(
    premisesRunningCosts = Some(3123.21),
    repairsAndMaintenance = Some(928.42),
    financialCosts = Some(842.99),
    professionalFees = Some(8831.12),
    costOfServices = Some(484.12),
    other = Some(992.82),
    travelCosts = Some(999.99),
    consolidatedExpenses = None,
    rentARoom = Some(UkPropertyExpensesRentARoom(
      Some(8842.43)
    )))
  val consolidatedExpenses:UkFhlPieExpenses = UkFhlPieExpenses(None, None, None, None, None, None, None, Some(22.50), None )

  val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(expenses))
  val consolidatedBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(consolidatedExpenses))

  val requestData: CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(
    nino, requestBody)
  val consolidatedRequestData:  CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(
    nino, consolidatedBody)

  val responseData = CreateHistoricFhlUkPiePeriodSummaryResponse(transactionReference)

  trait Test extends MockCreateHistoricFhlUkPiePeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateHistoricFhlUkPiePeriodSummaryService(
      connector = mockCreateHistoricFhlUkPiePropertyConnector
    )
  }

  "service" when {
    "service call successful" should {
      "return mapped result for regular period summary" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryConnector.createPropertyPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(transactionReference, responseData))))
      }
    }
  }
  "service" when {
    "service call successful" should {
      "return mapped result for consolidated expenses period summary" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryConnector.createPropertyPeriodSummary(consolidatedRequestData)
          .returns(Future.successful(Right(ResponseWrapper(transactionReference, responseData))))
      }
    }
  }
}
