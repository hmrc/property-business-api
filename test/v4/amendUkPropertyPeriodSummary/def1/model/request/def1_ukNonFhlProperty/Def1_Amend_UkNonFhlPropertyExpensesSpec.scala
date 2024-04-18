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

package v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty

import play.api.libs.json.Json
import support.UnitSpec
import v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom.Def1_Amend_UkPropertyExpensesRentARoom

class Def1_Amend_UkNonFhlPropertyExpensesSpec extends UnitSpec {

  private val requestBody =
    Def1_Amend_UkNonFhlPropertyExpenses(
      Some(41.12),
      Some(84.31),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(84.31),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(
        Def1_Amend_UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
      None
    )

  private val mtdJson = Json.parse("""
      |{
      |    "premisesRunningCosts": 41.12,
      |    "repairsAndMaintenance": 84.31,
      |    "financialCosts": 9884.93,
      |    "professionalFees": 842.99,
      |    "costOfServices": 31.44,
      |    "other": 84.31,
      |    "residentialFinancialCost": 9884.93,
      |    "travelCosts": 842.99,
      |    "residentialFinancialCostsCarriedForward": 31.44,
      |    "rentARoom": {
      |        "amountClaimed": 947.66
      |    }
      |}
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      |{
      |    "premisesRunningCosts": 41.12,
      |    "repairsAndMaintenance": 84.31,
      |    "financialCosts": 9884.93,
      |    "professionalFees": 842.99,
      |    "costOfServices": 31.44,
      |    "other": 84.31,
      |    "residentialFinancialCost": 9884.93,
      |    "travelCosts": 842.99,
      |    "residentialFinancialCostsCarriedForward": 31.44,
      |    "ukOtherRentARoom": {
      |        "amountClaimed": 947.66
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def1_Amend_UkNonFhlPropertyExpenses] shouldBe requestBody
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
