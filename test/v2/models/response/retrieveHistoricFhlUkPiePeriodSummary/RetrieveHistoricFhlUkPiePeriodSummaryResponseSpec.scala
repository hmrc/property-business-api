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
      |            "amount": 5000.99,
      |            "taxDeducted": 5000.99
      |         },
      |         "premiumsOfLeaseGrant": 5000.99,
      |         "reversePremiums": 5000.99,
      |         "otherIncome": 5000.99,
      |        "ukRentARoom": {
      |            "rentsReceived": 5000.99
      |         }
      |      },
      |      "deductions": {
      |         "premisesRunningCosts": 5000.99,
      |         "repairsAndMaintenance": 5000.99,
      |         "financialCosts": 5000.99,
      |         "professionalFees": 5000.99,
      |         "costOfServices": 5000.99,
      |         "other": 5000.99,
      |         "consolidatedExpenses": 5000.99,
      |         "residentialFinancialCost": 5000.99,
      |         "travelCosts": 5000.99,
      |         "residentialFinancialCostsCarriedForward": 5000.99,
      |         "ukRentARoom": {
      |            "amountClaimed": 5000.99
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
