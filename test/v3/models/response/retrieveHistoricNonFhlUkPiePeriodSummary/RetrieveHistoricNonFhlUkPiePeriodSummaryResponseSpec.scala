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

package v3.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import config.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RetrieveHistoricNonFhlUkPiePeriodSummaryResponseSpec extends UnitSpec with MockAppConfig {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val periodIncome: PeriodIncome =
    PeriodIncome(
      periodAmount = decimal("5000.99"),
      premiumsOfLeaseGrant = decimal("4999.99"),
      reversePremiums = decimal("4998.99"),
      otherIncome = decimal("4997.99"),
      taxDeducted = decimal("4996.99"),
      rentARoom = Option(RentARoomIncome(Some(4995.99)))
    )

  val periodExpenses: PeriodExpenses =
    PeriodExpenses(
      decimal("5000.99"),
      decimal("4999.99"),
      decimal("4998.99"),
      decimal("4997.99"),
      decimal("4996.99"),
      decimal("4995.99"),
      decimal("4994.99"),
      decimal("4993.99"),
      decimal("4992.99"),
      decimal("4991.99"),
      Option(RentARoomExpenses(Some(4990.99)))
    )

  val model: RetrieveHistoricNonFhlUkPiePeriodSummaryResponse = RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(
    fromDate = "2021-01-01",
    toDate = "2021-02-01",
    income = Some(
      periodIncome
    ),
    expenses = Some(
      periodExpenses
    )
  )

  val readsJson: JsValue = Json.parse("""{
                                        |   "from": "2021-01-01",
                                        |   "to": "2021-02-01",
                                        |   "financials": {
                                        |      "incomes": {
                                        |        "rentIncome": {
                                        |            "amount": 5000.99,
                                        |            "taxDeducted": 4996.99
                                        |        },
                                        |        "premiumsOfLeaseGrant": 4999.99,
                                        |        "reversePremiums": 4998.99,
                                        |        "otherIncome": 4997.99,
                                        |        "ukRentARoom": {
                                        |            "rentsReceived": 4995.99
                                        |         }
                                        |      },
                                        |      "deductions": {
                                                      "premisesRunningCosts": 5000.99,
                                        |         "repairsAndMaintenance": 4999.99,
                                        |         "financialCosts": 4998.99,
                                        |         "professionalFees": 4997.99,
                                        |         "costOfServices": 4996.99,
                                        |         "other": 4995.99,
                                        |         "consolidatedExpenses": 4994.99,
                                        |         "travelCosts": 4993.99,
                                        |         "residentialFinancialCost": 4992.99,
                                        |         "residentialFinancialCostsCarriedForward": 4991.99,
                                        |         "ukRentARoom": {
                                        |            "amountClaimed": 4990.99
                                        |         }
                                        |      }
                                        |   }
                                        |}
                                        |""".stripMargin)

  val writesJson: JsValue = Json.parse("""{
                                         |      "fromDate": "2021-01-01",
                                         |      "toDate": "2021-02-01",
                                         |      "income": {
                                         |        "periodAmount": 5000.99,
                                         |        "premiumsOfLeaseGrant": 4999.99,
                                         |        "reversePremiums": 4998.99,
                                         |        "otherIncome": 4997.99,
                                         |        "taxDeducted": 4996.99,
                                         |        "rentARoom":{
                                         |          "rentsReceived": 4995.99
                                         |        }
                                         |      },
                                         |      "expenses": {
                                         |          "premisesRunningCosts": 5000.99,
                                         |          "repairsAndMaintenance": 4999.99,
                                         |          "financialCosts": 4998.99,
                                         |          "professionalFees": 4997.99,
                                         |          "costOfServices": 4996.99,
                                         |          "other": 4995.99,
                                         |          "consolidatedExpenses": 4994.99,
                                         |          "travelCosts": 4993.99,
                                         |          "residentialFinancialCost": 4992.99,
                                         |          "residentialFinancialCostsCarriedForward": 4991.99,
                                         |          "rentARoom":{
                                         |              "amountClaimed":4990.99
                                         |          }
                                         |      }
                                         |}
                                         |""".stripMargin)

  "reads" should {
    "read the JSON object from downstream into a case class" in {
      val result = readsJson.as[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]
      result shouldBe model
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe writesJson
      }
    }
  }

}
