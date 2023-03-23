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

package v2.connectors

import api.connectors.ConnectorSpec
import api.models.domain.Nino
import api.models.ResponseWrapper
import v2.models.request.common.ukFhlPieProperty.{UkFhlPieExpenses, UkFhlPieIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryRequest, CreateHistoricFhlUkPiePeriodSummaryRequestBody}

import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino     = "WE123567A"
  val fromDate = "2021-01-06"
  val toDate   = "2021-02-06"

  val income: UkFhlPieIncome = UkFhlPieIncome(Some(129.10), Some(129.11), Some(UkPropertyIncomeRentARoom(Some(144.23))))

  val expenses: UkFhlPieExpenses = UkFhlPieExpenses(Some(3123.21),
                                                    Some(928.42),
                                                    Some(842.99),
                                                    Some(8831.12),
                                                    Some(484.12),
                                                    Some(992.82),
                                                    Some(999.99),
                                                    None,
                                                    Some(
                                                      UkPropertyExpensesRentARoom(
                                                        Some(8842.43)
                                                      )))

  val consolidatedExpenses: UkFhlPieExpenses = UkFhlPieExpenses(None, None, None, None, None, None, None, Some(22.50), None)

  val url: String = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries"

  val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(expenses))

  val consolidatedBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, Some(income), Some(consolidatedExpenses))

  val requestData: CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), requestBody)

  val consolidatedRequestData: CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), consolidatedBody)

  trait Test {
    _: ConnectorTest =>

    val connector: CreateHistoricFhlUkPiePeriodSummaryConnector = new CreateHistoricFhlUkPiePeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {

    "post a body with dates, income and expenses and return a 202 with the Period ID added" in new IfsTest with Test {
      val downstreamOutcome = Right(ResponseWrapper(correlationId, ()))

      willPost(
        url = url,
        body = requestBody
      ).returns(Future.successful(downstreamOutcome))

      val result = await(connector.createPeriodSummary(requestData))
      result shouldBe downstreamOutcome
    }

    "post a body with dates, income and consolidated expenses and return a 202 with the Period ID added" in new IfsTest with Test {
      val downstreamOutcome = Right(ResponseWrapper(correlationId, ()))

      willPost(
        url = url,
        body = consolidatedBody
      ).returns(Future.successful(downstreamOutcome))

      await(connector.createPeriodSummary(consolidatedRequestData)) shouldBe downstreamOutcome
    }
  }
}
