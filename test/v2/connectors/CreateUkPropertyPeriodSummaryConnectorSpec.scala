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

import v2.models.domain.{Nino, TaxYear}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String = "AA123456A"
  val taxYear: String = "2022-23"

  val body: CreateUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(UkFhlProperty(
      Some(UkFhlPropertyIncome(
        Some(5000.99),
        Some(3123.21),
        Some(UkPropertyIncomeRentARoom(
          Some(532.12)
        ))
      )),
      Some(UkFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.52),
        consolidatedExpenses = None,
        Some(974.47),
        Some(UkPropertyExpensesRentARoom(
          Some(8842.43)
        ))
      ))
    )),
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
        Some(UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
      )),
      Some(UkNonFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.00),
        Some(999.99),
        Some(974.47),
        Some(8831.12),
        Some(UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
        consolidatedExpenses = None
      ))
    ))
  )


  private val response = CreateUkPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val requestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), businessId, body)

  trait Test {
    _: ConnectorTest =>
    val connector: CreateUkPropertyPeriodSummaryConnector = new CreateUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "post a body with regular expenses and return 200 with submissionId" in new IfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      willPost(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2022-23&incomeSourceId=$businessId",
          body = body
        )
        .returns(Future.successful(outcome))

      await(connector.createUkProperty(requestData)) shouldBe outcome

    }
  }
}
