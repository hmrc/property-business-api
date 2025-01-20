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

package v6.retrieveUkPropertyCumulativeSummary.def1.model

import shared.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v6.retrieveUkPropertyCumulativeSummary.def1.model.response._

trait Def1_RetrieveUkPropertyCumulativeSummaryFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "ukOtherRentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 4.11,
      |      "repairsAndMaintenance": 4.12,
      |      "financialCosts": 4.13,
      |      "professionalFees": 4.14,
      |      "costOfServices": 4.15,
      |      "other": 4.16,
      |      "residentialFinancialCost": 4.18,
      |      "travelCosts": 4.19,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "ukOtherRentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val consolidatedDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "ukOtherRentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 4.17,
      |      "residentialFinancialCostAmount": 4.18,
      |      "broughtFwdResidentialFinancialCostAmount": 4.20,
      |      "ukOtherRentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "rentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 4.11,
      |      "repairsAndMaintenance": 4.12,
      |      "financialCosts": 4.13,
      |      "professionalFees": 4.14,
      |      "costOfServices": 4.15,
      |      "other": 4.16,
      |      "residentialFinancialCost": 4.18,
      |      "travelCosts": 4.19,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "rentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val consolidatedMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "rentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 4.17,
      |      "residentialFinancialCost": 4.18,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "rentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val rentARoomIncome: RentARoomIncome = RentARoomIncome(
    rentsReceived = Some(3.16)
  )

  val income: Income = Income(
    premiumsOfLeaseGrant = Some(3.11),
    reversePremiums = Some(3.12),
    periodAmount = Some(3.13),
    taxDeducted = Some(3.14),
    otherIncome = Some(3.15),
    rentARoom = Some(rentARoomIncome)
  )

  val rentARoomExpenses: RentARoomExpenses = RentARoomExpenses(
    amountClaimed = Some(4.21)
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

  val ukProperty: UkProperty = UkProperty(
    income = Some(income),
    expenses = Some(expenses)
  )

  val ukPropertyConsolidated: UkProperty = UkProperty(
    income = Some(income),
    expenses = Some(expensesConsolidated)
  )

  val fullResponse: Def1_RetrieveUkPropertyCumulativeSummaryResponse = Def1_RetrieveUkPropertyCumulativeSummaryResponse(
    submittedOn = Timestamp("2025-06-17T10:53:38.000Z"),
    fromDate = "2024-01-29",
    toDate = "2025-03-29",
    ukProperty = Some(ukProperty)
  )

}
