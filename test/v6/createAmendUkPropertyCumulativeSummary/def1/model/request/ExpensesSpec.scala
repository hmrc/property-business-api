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

package v6.createAmendUkPropertyCumulativeSummary.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class ExpensesSpec extends UnitSpec {

  val consolidatedDownstreamRequest: JsValue = Json.parse(
    """{
      |  "consolidatedExpenses": 4.17,
      |  "residentialFinancialCostAmount": 4.18,
      |  "broughtFwdResidentialFinancialCostAmount": 4.20,
      |  "ukOtherRentARoom": {
      |      "amountClaimed": 4.21
      |  }
      |}
    """.stripMargin
  )

  val consolidatedVendorRequestJson: JsValue = Json.parse(
    """
      |{
      | "consolidatedExpenses": 4.17,
      | "residentialFinancialCost": 4.18,
      | "residentialFinancialCostsCarriedForward": 4.20,
      | "rentARoom": {
      |    "amountClaimed": 4.21
      |  }
      |}
    """.stripMargin
  )

  val fullExpensesVendorRequestJson: JsValue = Json.parse(
    """{
      |  "premisesRunningCosts": 4.11,
      |  "repairsAndMaintenance": 4.12,
      |  "financialCosts": 4.13,
      |  "professionalFees": 4.14,
      |  "costOfServices": 4.15,
      |  "other": 4.16,
      |  "residentialFinancialCost": 4.18,
      |  "travelCosts": 4.19,
      |  "residentialFinancialCostsCarriedForward": 4.20,
      |  "rentARoom": {
      |    "amountClaimed": 4.21
      |  }
      |}
    """.stripMargin
  )

  val fullExpensesDownstreamRequest: JsValue = Json.parse(
    """{
      |  "premisesRunningCosts": 4.11,
      |  "repairsAndMaintenance": 4.12,
      |  "financialCosts": 4.13,
      |  "professionalFees": 4.14,
      |  "costOfServices": 4.15,
      |  "other": 4.16,
      |  "travelCosts": 4.19,
      |  "residentialFinancialCost": 4.18,
      |  "residentialFinancialCostsCarriedForward": 4.20,
      |  "ukOtherRentARoom": {
      |    "amountClaimed": 4.21
      |  }
      |}
    """.stripMargin
  )

  val rentARoomExpenses: RentARoomExpenses = RentARoomExpenses(
    amountClaimed = Some(4.21)
  )

  val expensesConsolidated: Expenses = Expenses(
    premisesRunningCosts = None,
    repairsAndMaintenance = None,
    financialCosts = None,
    professionalFees = None,
    costOfServices = None,
    other = None,
    consolidatedExpenses = Some(4.17),
    residentialFinancialCost = Some(4.18),
    travelCosts = None,
    residentialFinancialCostsCarriedForward = Some(4.20),
    rentARoom = Some(rentARoomExpenses)
  )

  val expenses: Expenses = Expenses(
    premisesRunningCosts = Some(4.11),
    repairsAndMaintenance = Some(4.12),
    financialCosts = Some(4.13),
    professionalFees = Some(4.14),
    costOfServices = Some(4.15),
    other = Some(4.16),
    consolidatedExpenses = None,
    residentialFinancialCost = Some(4.18),
    travelCosts = Some(4.19),
    residentialFinancialCostsCarriedForward = Some(4.20),
    rentARoom = Some(rentARoomExpenses)
  )

  "Expenses" when {

    "consolidated" must {
      "read from valid JSON" should {
        "return the parsed object when the request contains consolidated expenses" in {
          consolidatedVendorRequestJson.as[Expenses] shouldBe expensesConsolidated
        }
      }

      "write to JSON" should {
        "return the expected JSON when it is a consolidated request" in {
          Json.toJson(expensesConsolidated) shouldBe consolidatedDownstreamRequest
        }
      }
    }

    "not consolidated" must {

      "read from valid JSON" should {
        "return the parsed object" in {
          fullExpensesVendorRequestJson.as[Expenses] shouldBe expenses
        }
      }

      "write to JSON" should {
        "return the expected JSON" in {
          Json.toJson(expenses) shouldBe fullExpensesDownstreamRequest
        }
      }
    }

  }

}
