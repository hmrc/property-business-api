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

package v3.models.request.createUkPropertyPeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v3.models.request.common.ukFhlProperty._
import v3.models.request.common.ukNonFhlProperty._
import v3.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

class CreateUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec {

  val requestBody: CreateUkPropertyPeriodSummaryRequestBody =
    CreateUkPropertyPeriodSummaryRequestBody(
      "2020-01-01",
      "2020-01-31",
      Some(
        UkFhlProperty(
          Some(
            UkFhlPropertyIncome(
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
            Some(99282),
            Some(999.99),
            Some(974.47),
            Some(UkPropertyExpensesRentARoom(
              Some(8842.43)
            ))
          ))
        )),
      Some(
        UkNonFhlProperty(
          Some(
            UkNonFhlPropertyIncome(
              Some(41.12),
              Some(84.31),
              Some(9884.93),
              Some(842.99),
              Some(31.44),
              Some(UkPropertyIncomeRentARoom(
                Some(947.66)
              ))
            )),
          Some(
            UkNonFhlPropertyExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              Some(988.18)
            ))
        ))
    )

  val mtdJson: JsValue = Json.parse("""
                                      |{
                                      |    "fromDate": "2020-01-01",
                                      |    "toDate": "2020-01-31",
                                      |    "ukFhlProperty":{
                                      |        "income": {
                                      |            "periodAmount": 5000.99,
                                      |            "taxDeducted": 3123.21,
                                      |            "rentARoom": {
                                      |                "rentsReceived": 532.12
                                      |            }
                                      |        },
                                      |        "expenses": {
                                      |            "premisesRunningCosts": 3123.21,
                                      |            "repairsAndMaintenance": 928.42,
                                      |            "financialCosts": 842.99,
                                      |            "professionalFees": 8831.12,
                                      |            "costOfServices": 484.12,
                                      |            "other": 99282,
                                      |            "consolidatedExpenses": 999.99,
                                      |            "travelCosts": 974.47,
                                      |            "rentARoom": {
                                      |                "amountClaimed": 8842.43
                                      |            }
                                      |        }
                                      |    },
                                      |    "ukNonFhlProperty": {
                                      |        "income": {
                                      |            "premiumsOfLeaseGrant": 41.12,
                                      |            "reversePremiums": 84.31,
                                      |            "periodAmount": 9884.93,
                                      |            "taxDeducted": 842.99,
                                      |            "otherIncome": 31.44,
                                      |            "rentARoom": {
                                      |                "rentsReceived": 947.66
                                      |            }
                                      |        },
                                      |        "expenses": {
                                      |            "consolidatedExpenses": 988.18
                                      |        }
                                      |    }
                                      |}
                                      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
                                      |{
                                      |    "fromDate": "2020-01-01",
                                      |    "toDate": "2020-01-31",
                                      |    "ukFhlProperty":{
                                      |        "income": {
                                      |            "periodAmount": 5000.99,
                                      |            "taxDeducted": 3123.21,
                                      |            "ukFhlRentARoom": {
                                      |                "rentsReceived": 532.12
                                      |            }
                                      |        },
                                      |        "expenses": {
                                      |            "premisesRunningCosts": 3123.21,
                                      |            "repairsAndMaintenance": 928.42,
                                      |            "financialCosts": 842.99,
                                      |            "professionalFees": 8831.12,
                                      |            "costOfServices": 484.12,
                                      |            "other": 99282,
                                      |            "consolidatedExpenses": 999.99,
                                      |            "travelCosts": 974.47,
                                      |            "ukFhlRentARoom": {
                                      |                "amountClaimed": 8842.43
                                      |            }
                                      |        }
                                      |    },
                                      |    "ukOtherProperty": {
                                      |        "income": {
                                      |            "premiumsOfLeaseGrant": 41.12,
                                      |            "reversePremiums": 84.31,
                                      |            "periodAmount": 9884.93,
                                      |            "taxDeducted": 842.99,
                                      |            "otherIncome": 31.44,
                                      |            "ukOtherRentARoom": {
                                      |                "rentsReceived": 947.66
                                      |            }
                                      |        },
                                      |        "expenses": {
                                      |            "consolidatedExpenses": 988.18
                                      |        }
                                      |    }
                                      |}
                                      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[CreateUkPropertyPeriodSummaryRequestBody] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe desJson
      }
    }
  }

}
