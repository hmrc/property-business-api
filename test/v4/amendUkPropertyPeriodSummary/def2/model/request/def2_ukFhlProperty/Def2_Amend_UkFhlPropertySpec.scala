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

package v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.*

class Def2_Amend_UkFhlPropertySpec extends UnitSpec {

  val requestBody: Def2_Amend_UkFhlProperty =
    Def2_Amend_UkFhlProperty(
      Some(
        Def2_Amend_UkFhlPropertyIncome(
          Some(5000.99),
          Some(3123.21),
          Some(
            Def2_Amend_UkPropertyIncomeRentARoom(
              Some(532.12)
            ))
        )),
      Some(
        Def2_Amend_UkFhlPropertyExpenses(
          Some(3123.21),
          Some(928.42),
          Some(842.99),
          Some(8831.12),
          Some(484.12),
          Some(99282),
          Some(999.99),
          Some(974.47),
          Some(
            Def2_Amend_UkPropertyExpensesRentARoom(
              Some(8842.43)
            ))
        ))
    )

  val mtdJson: JsValue = Json.parse("""
      |{
      |    "income": {
      |        "periodAmount": 5000.99,
      |        "taxDeducted": 3123.21,
      |        "rentARoom": {
      |            "rentsReceived": 532.12
      |        }
      |    },
      |    "expenses": {
      |        "premisesRunningCosts": 3123.21,
      |        "repairsAndMaintenance": 928.42,
      |        "financialCosts": 842.99,
      |        "professionalFees": 8831.12,
      |        "costOfServices": 484.12,
      |        "other": 99282,
      |        "consolidatedExpenses": 999.99,
      |        "travelCosts": 974.47,
      |        "rentARoom": {
      |           "amountClaimed": 8842.43
      |        }
      |    }
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      |    "income": {
      |        "periodAmount": 5000.99,
      |        "taxDeducted": 3123.21,
      |        "ukFhlRentARoom": {
      |            "rentsReceived": 532.12
      |        }
      |    },
      |    "expenses": {
      |        "premisesRunningCosts": 3123.21,
      |        "repairsAndMaintenance": 928.42,
      |        "financialCosts": 842.99,
      |        "professionalFees": 8831.12,
      |        "costOfServices": 484.12,
      |        "consolidatedExpense": 999.99,
      |        "other": 99282,
      |        "travelCosts": 974.47,
      |        "ukFhlRentARoom": {
      |           "amountClaimed": 8842.43
      |        }
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def2_Amend_UkFhlProperty] shouldBe requestBody
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
