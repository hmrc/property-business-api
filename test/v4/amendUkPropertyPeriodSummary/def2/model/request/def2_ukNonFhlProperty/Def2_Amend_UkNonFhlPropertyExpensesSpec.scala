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

package v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty

import play.api.libs.json.Json
import support.UnitSpec
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Amend_UkPropertyExpensesRentARoom

class Def2_Amend_UkNonFhlPropertyExpensesSpec extends UnitSpec {

  private val requestBody =
    Def2_Amend_UkNonFhlPropertyExpenses(
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
        Def2_Amend_UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
      None
    )

  private val requestBodySubmission =
    Def2_Amend_UkNonFhlPropertyExpensesSubmission(
      Some(41.12),
      Some(84.31),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(84.31),
      Some(9884.93),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(31.44),
      Some(
        Def2_Amend_UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
      None
    )

  private val requestBodySubmissionConsolidated =
    Def2_Amend_UkNonFhlPropertyExpensesSubmission(
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      Some(9884.93),
      None,
      None,
      Some(31.44),
      Some(
        Def2_Amend_UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
      Some(100.00)
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

  private val mtdJsonSubmission = Json.parse("""
      |{
      |    "premisesRunningCosts": 41.12,
      |    "repairsAndMaintenance": 84.31,
      |    "financialCosts": 9884.93,
      |    "professionalFees": 842.99,
      |    "costOfServices": 31.44,
      |    "other": 84.31,
      |    "residentialFinancialCost": 9884.93,
      |    "residentialFinancialCostAmount": 9884.93,
      |    "travelCosts": 842.99,
      |    "residentialFinancialCostsCarriedForward": 31.44,
      |    "broughtFwdResidentialFinancialCostAmount": 31.44,
      |    "rentARoom": {
      |        "amountClaimed": 947.66
      |    }
      |}
      |""".stripMargin)

  private val mtdJsonSubmissionConsolidated = Json.parse("""
      |{
      |    "residentialFinancialCostAmount": 9884.93,
      |    "broughtFwdResidentialFinancialCostAmount": 31.44,
      |    "rentARoom": {
      |        "amountClaimed": 947.66
      |    },
      |    "consolidatedExpenses" : 100.00
      |}
      |""".stripMargin)

  private val downstreamJsonSubmission = Json.parse("""
      |{
      |    "premisesRunningCosts": 41.12,
      |    "repairsAndMaintenance": 84.31,
      |    "financialCosts": 9884.93,
      |    "professionalFees": 842.99,
      |    "costOfServices": 31.44,
      |    "other": 84.31,
      |    "residentialFinancialCost": 9884.93,
      |    "residentialFinancialCostAmount": 9884.93,
      |    "travelCosts": 842.99,
      |    "residentialFinancialCostsCarriedForward": 31.44,
      |    "broughtFwdResidentialFinancialCostAmount": 31.44,
      |    "ukOtherRentARoom": {
      |        "amountClaimed": 947.66
      |    }
      |}
      |""".stripMargin)

  "Def2_Amend_UkNonFhlPropertyExpenses" when {
    ".toSubmissionModel" should {
      "return as Def2_Amend_UkNonFhlPropertyExpensesSubmission with correct values" in {
        requestBody.toSubmissionModel shouldBe mtdJson.as[Def2_Amend_UkNonFhlPropertyExpensesSubmission]
        requestBodySubmissionConsolidated shouldBe mtdJsonSubmissionConsolidated.as[Def2_Amend_UkNonFhlPropertyExpensesSubmission]
      }
    }
  }

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def2_Amend_UkNonFhlPropertyExpenses] shouldBe requestBody
        mtdJsonSubmission.as[Def2_Amend_UkNonFhlPropertyExpensesSubmission] shouldBe requestBodySubmission
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe downstreamJson
        Json.toJson(requestBodySubmission) shouldBe downstreamJsonSubmission
      }
    }
  }

}
