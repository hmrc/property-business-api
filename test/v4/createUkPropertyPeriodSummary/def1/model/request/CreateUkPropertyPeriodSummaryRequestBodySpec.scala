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

package v4.createUkPropertyPeriodSummary.def1.model.request

import play.api.libs.json.Json
import support.UnitSpec
import v4.createUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty._
import v4.createUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty._
import v4.createUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom._
import v4.createUkPropertyPeriodSummary.model.request._

class CreateUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec {

  private val requestBody =
    Def1_CreateUkPropertyPeriodSummaryRequestBody(
      "2020-01-01",
      "2020-01-31",
      Some(
        Def1_Create_UkFhlProperty(
          Some(
            Def1_Create_UkFhlPropertyIncome(
              Some(5000.99),
              Some(3123.21),
              Some(Def1_Create_UkPropertyIncomeRentARoom(
                Some(532.12)
              ))
            )),
          Some(Def1_Create_UkFhlPropertyExpenses(
            Some(3123.21),
            Some(928.42),
            Some(842.99),
            Some(8831.12),
            Some(484.12),
            Some(99282),
            Some(999.99),
            Some(974.47),
            Some(Def1_Create_UkPropertyExpensesRentARoom(
              Some(8842.43)
            ))
          ))
        )),
      Some(
        Def1_Create_UkNonFhlProperty(
          Some(
            Def1_Create_UkNonFhlPropertyIncome(
              Some(41.12),
              Some(84.31),
              Some(9884.93),
              Some(842.99),
              Some(31.44),
              Some(Def1_Create_UkPropertyIncomeRentARoom(
                Some(947.66)
              ))
            )),
          Some(
            Def1_Create_UkNonFhlPropertyExpenses(
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

  private val mtdJson = Json.parse("""
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

  private val downstreamJson = Json.parse("""
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
        mtdJson.as[Def1_CreateUkPropertyPeriodSummaryRequestBody] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe downstreamJson
      }
    }
  }

}
