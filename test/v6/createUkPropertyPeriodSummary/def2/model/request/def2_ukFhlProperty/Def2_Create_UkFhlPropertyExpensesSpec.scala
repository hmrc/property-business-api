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

package v6.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v6.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty.Def2_Create_UkFhlPropertyExpenses
import v6.createUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Create_UkPropertyExpensesRentARoom

class Def2_Create_UkFhlPropertyExpensesSpec extends UnitSpec {

  // @formatter:off
  private val requestBody = {
    Def2_Create_UkFhlPropertyExpenses(
      Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
      Some(484.12), Some(99282), Some(999.99), Some(974.47),
      Some(Def2_Create_UkPropertyExpensesRentARoom(Some(8842.43)))
    )
  }
  // @formatter:on

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
    |    "consolidatedExpenses": 999.99,
    |    "travelCosts": 974.47,
    |    "ukFhlRentARoom": {
    |        "amountClaimed": 8842.43
    |    }
    |}
    |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def2_Create_UkFhlPropertyExpenses] shouldBe requestBody
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
