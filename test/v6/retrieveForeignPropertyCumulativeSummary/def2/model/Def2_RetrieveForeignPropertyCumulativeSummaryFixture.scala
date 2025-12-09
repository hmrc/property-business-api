/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyCumulativeSummary.def2.model

import shared.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.response._

object Def2_RetrieveForeignPropertyCumulativeSummaryFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-06-17T10:53:38.000Z",
      |  "fromDate": "2026-01-29",
      |  "toDate": "2027-03-29",
      |  "foreignProperty": [
      |    {
      |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrantAmount": 950.48,
      |        "otherPropertyIncomeAmount": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "premisesRunningCostsAmount": -4929.50,
      |        "repairsAndMaintenanceAmount": -54.30,
      |        "financialCostsAmount": 2090.35,
      |        "professionalFeesAmount": -90.20,
      |        "travelCostsAmount": 560.99,
      |        "costOfServicesAmount": -100.83,
      |        "residentialFinancialCostAmount": 857.78,
      |        "broughtFwdResidentialFinancialCostAmount": 600.10,
      |        "otherAmount": 334.64
      |      }
      |    }
      |  ]
      |}
  """.stripMargin
  )

  val consolidatedDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-06-17T10:53:38.000Z",
      |  "fromDate": "2026-01-29",
      |  "toDate": "2027-03-29",
      |  "foreignProperty": [
      |    {
      |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrantAmount": 950.48,
      |        "otherPropertyIncomeAmount": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "residentialFinancialCostAmount": 857.78,
      |        "broughtFwdResidentialFinancialCostAmount": 600.10,
      |        "consolidatedExpenseAmount": 1500.50
      |      }
      |    }
      |  ]
      |}
  """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-06-17T10:53:38.000Z",
      |  "fromDate": "2026-01-29",
      |  "toDate": "2027-03-29",
      |  "foreignProperty": [
      |    {
      |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": -4929.50,
      |        "repairsAndMaintenance": -54.30,
      |        "financialCosts": 2090.35,
      |        "professionalFees": -90.20,
      |        "travelCosts": 560.99,
      |        "costOfServices": -100.83,
      |        "residentialFinancialCost": 857.78,
      |        "broughtFwdResidentialFinancialCost": 600.10,
      |        "other": 334.64
      |      }
      |    }
      |  ]
      |}
  """.stripMargin
  )

  val consolidatedMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-06-17T10:53:38.000Z",
      |  "fromDate": "2026-01-29",
      |  "toDate": "2027-03-29",
      |  "foreignProperty": [
      |    {
      |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "residentialFinancialCost": 857.78,
      |        "broughtFwdResidentialFinancialCost": 600.10,
      |        "consolidatedExpenses": 1500.50
      |      }
      |    }
      |  ]
      |}
  """.stripMargin
  )

  val rentIncome: RentIncome = RentIncome(
    rentAmount = Some(440.31)
  )

  val income: Income = Income(
    rentIncome = Some(rentIncome),
    foreignTaxCreditRelief = Some(false),
    premiumsOfLeaseGrant = Some(950.48),
    otherPropertyIncome = Some(802.49),
    foreignTaxPaidOrDeducted = Some(734.18),
    specialWithholdingTaxOrUkTaxPaid = Some(85.47)
  )

  val expenses: Expenses = Expenses(
    premisesRunningCosts = Some(-4929.50),
    repairsAndMaintenance = Some(-54.30),
    financialCosts = Some(2090.35),
    professionalFees = Some(-90.20),
    travelCosts = Some(560.99),
    costOfServices = Some(-100.83),
    residentialFinancialCost = Some(857.78),
    broughtFwdResidentialFinancialCost = Some(600.10),
    other = Some(334.64),
    consolidatedExpenses = None
  )

  val expensesConsolidated: Expenses = Expenses(
    premisesRunningCosts = None,
    repairsAndMaintenance = None,
    financialCosts = None,
    professionalFees = None,
    travelCosts = None,
    costOfServices = None,
    residentialFinancialCost = Some(857.78),
    broughtFwdResidentialFinancialCost = Some(600.10),
    other = None,
    consolidatedExpenses = Some(1500.50)
  )

  val foreignPropertyEntry: ForeignPropertyEntry = ForeignPropertyEntry(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    income = Some(income),
    expenses = Some(expenses)
  )

  val foreignPropertyConsolidatedEntry: ForeignPropertyEntry = ForeignPropertyEntry(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    income = Some(income),
    expenses = Some(expensesConsolidated)
  )

  val fullResponse: Def2_RetrieveForeignPropertyCumulativeSummaryResponse = Def2_RetrieveForeignPropertyCumulativeSummaryResponse(
    submittedOn = Timestamp("2026-06-17T10:53:38.000Z"),
    fromDate = "2026-01-29",
    toDate = "2027-03-29",
    foreignProperty = Seq(foreignPropertyEntry)
  )

}
