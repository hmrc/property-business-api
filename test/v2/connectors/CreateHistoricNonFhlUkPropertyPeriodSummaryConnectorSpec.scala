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

package v2.connectors

import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary._

import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String = "TC663795B"
  val fromDate     = "2021-01-06"
  val toDate       = "2021-02-06"

  val income: UkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(Some(2355.45), Some(454.56), Some(123.45), Some(234.53), Some(567.89), Some(UkPropertyIncomeRentARoom(Some(567.56))))

  val expenses: UkNonFhlPropertyExpenses = UkNonFhlPropertyExpenses(
    Some(567.53),
    Some(324.65),
    Some(453.56),
    Some(535.78),
    Some(678.34),
    Some(682.34),
    Some(1000.45),
    Some(645.56),
    Some(672.34),
    Some(
      UkPropertyExpensesRentARoom(
        Some(545.9)
      )
    ),
    None
  )

  val url: String = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries"

  val requestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      fromDate,
      toDate,
      Some(income),
      Some(expenses)
    )

  val requestData: CreateHistoricNonFhlUkPropertyPeriodSummaryRequest =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), requestBody)

  trait Test {
    _: ConnectorTest =>

    val connector: CreateHistoricNonFhlUkPropertyPeriodSummaryConnector = new CreateHistoricNonFhlUkPropertyPeriodSummaryConnector(
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
        )
        .returns(Future.successful(downstreamOutcome))

      val result = await(connector.createPeriodSummary(requestData))
      result shouldBe downstreamOutcome
    }
  }

}
