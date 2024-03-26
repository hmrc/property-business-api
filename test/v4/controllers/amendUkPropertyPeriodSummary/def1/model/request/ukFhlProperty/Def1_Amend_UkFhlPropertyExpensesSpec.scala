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

package v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.ukFhlProperty

import play.api.libs.json.Json
import support.UnitSpec
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.Def1_Amend_UkFhlPropertyExpenses
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom.Def1_Amend_UkPropertyExpensesRentARoom

class Def1_Amend_UkFhlPropertyExpensesSpec extends UnitSpec {

  private val requestBody =
    Def1_Amend_UkFhlPropertyExpenses(
      Some(3123.21),
      Some(928.42),
      Some(842.99),
      Some(8831.12),
      Some(484.12),
      Some(99282),
      Some(999.99),
      Some(974.47),
      Some(
        Def1_Amend_UkPropertyExpensesRentARoom(
          Some(8842.43)
        ))
    )

  private val mtdJson = Json.parse("""
      |{
      |    "premisesRunningCosts": 3123.21,
      |    "repairsAndMaintenance": 928.42,
      |    "financialCosts": 842.99,
      |    "professionalFees": 8831.12,
      |    "costOfServices": 484.12,
      |    "other": 99282,
      |    "consolidatedExpenses": 999.99,
      |    "travelCosts": 974.47,
      |    "rentARoom": {
      |        "amountClaimed": 8842.43
      |    }
      |}
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      |{
      |    "premisesRunningCosts": 3123.21,
      |    "repairsAndMaintenance": 928.42,
      |    "financialCosts": 842.99,
      |    "professionalFees": 8831.12,
      |    "costOfServices": 484.12,
      |    "other": 99282,
      |    "consolidatedExpense": 999.99,
      |    "travelCosts": 974.47,
      |    "ukFhlRentARoom": {
      |        "amountClaimed": 8842.43
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def1_Amend_UkFhlPropertyExpenses] shouldBe requestBody
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
