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

package v2.models.response.retrieveHistoricFhlUkPiePeriodSummary

import mocks.MockAppConfig
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class RetrieveHistoricFhlUkPiePeriodSummaryResponseSpec extends UnitSpec with MockAppConfig {

  val downstreamJson: JsValue = Json.parse("""{
      |   "from": "2001-01-01",
      |   "to": "2001-01-01",
      |   "financials": {
      |      "incomes": {
      |         "rentIncome": {
      |            "amount": 200.00,
      |            "taxDeducted": 100.00
      |         },
      |         "premiumsOfLeaseGrant": 200.00,
      |         "reversePremiums": 200.00,
      |         "otherIncome": 200.00,
      |        "ukRentARoom": {
      |            "rentsReceived": 100.00
      |         }
      |      },
      |      "deductions": {
      |         "premisesRunningCosts": 200.00,
      |         "repairsAndMaintenance": 200.00,
      |         "financialCosts": 200.00,
      |         "professionalFees": 200.00,
      |         "costOfServices": 200.00,
      |         "other": 200.00,
      |         "consolidatedExpenses": 200.00,
      |         "residentialFinancialCost": 200.00,
      |         "travelCosts": 200.00,
      |         "residentialFinancialCostsCarriedForward": 200.00,
      |         "ukRentARoom": {
      |            "amountClaimed": 200.00
      |         }
      |      }
      |   }
      |}
      |""".stripMargin)

//  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val model = RetrieveHistoricFhlUkPiePeriodSummaryResponse(
    fromDate = "2001-01-01",
    toDate = "2001-01-01",
    income = Some(
      PeriodIncome(Some(5000.99), Some(5000.99), Option(RentARoomIncome(Some(5000.99))))
    ),
    expenses = Some(
      PeriodExpenses(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(RentARoomExpenses(Some(5000.99)))
      ))
  )

//  val model = RetrieveHistoricFhlUkPiePeriodSummaryResponse(
//    Some(
//      AnnualAdjustments(decimal("200.00"),
//                        decimal("300.00"),
//                        decimal("400.00"),
//                        true,
//                        decimal("500.02"),
//                        true,
//                        Option(RentARoom(jointlyLet = false)))),
//    Some(AnnualAllowances(decimal("200.00"), decimal("300.00"), decimal("400.02"), decimal("10.02")))
//  )

  "reads" should {
    "read the JSON object from downstream into a case class" in {
      val result = downstreamJson.as[RetrieveHistoricFhlUkPiePeriodSummaryResponse]
      result shouldBe model
    }
  }

}
