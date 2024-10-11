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

package v5.createForeignPropertyPeriodCumulativeSummary.def1.model

import play.api.libs.json.{JsValue, Json}
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.Def1_foreignPropertyEntry._
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody

trait Def1_CreateForeignPropertyPeriodCumulativeSummaryFixtures {

  private val foreignPropertyIncome = Some(
    Def1_Create_ForeignPropertyIncome(
      rentIncome = Some(Def1_Create_ForeignPropertyRentIncome(Some(1000.99))),
      foreignTaxCreditRelief = Some(false),
      premiumsOfLeaseGrant = Some(1000.99),
      otherPropertyIncome = Some(2000.99),
      foreignTaxPaidOrDeducted = Some(3000.99),
      specialWithholdingTaxOrUkTaxPaid = Some(4000.99)
    ))

  private val regularForeignPropertyExpenses = Some(
    Def1_Create_CreateForeignPropertyExpenses(
      premisesRunningCosts = Some(1000.99),
      repairsAndMaintenance = Some(2000.99),
      financialCosts = Some(3000.99),
      professionalFees = Some(4000.99),
      costOfServices = Some(5000.99),
      travelCosts = Some(6000.99),
      residentialFinancialCost = Some(7000.99),
      broughtFwdResidentialFinancialCost = Some(8000.99),
      other = Some(9000.99),
      consolidatedExpenses = None
    ))

  private val consolidatedForeignPropertyExpenses = Some(
    Def1_Create_CreateForeignPropertyExpenses(
      premisesRunningCosts = None,
      repairsAndMaintenance = None,
      financialCosts = None,
      professionalFees = None,
      costOfServices = None,
      travelCosts = None,
      residentialFinancialCost = None,
      broughtFwdResidentialFinancialCost = None,
      other = None,
      consolidatedExpenses = Some(1000.99)
    ))

  private val regularForeignProperty = Some(
    List(Def1_Create_CreateForeignPropertyEntry(countryCode = "FRA", income = foreignPropertyIncome, expenses = regularForeignPropertyExpenses)))

  private val consolidatedForeignProperty = Some(
    List(
      Def1_Create_CreateForeignPropertyEntry(
        countryCode = "FRA",
        income = foreignPropertyIncome,
        expenses = consolidatedForeignPropertyExpenses
      )))

  val regularExpensesRequestBody: Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody =
    Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody(
      fromDate = "2025-01-01",
      toDate = "2026-01-31",
      foreignProperty = regularForeignProperty
    )

  val consolidatedExpensesRequestBody: Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody =
    Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody(
      fromDate = "2025-01-01",
      toDate = "2026-01-31",
      foreignProperty = consolidatedForeignProperty
    )

  val regularMtdRequestJson: JsValue = Json.parse(
    """{
      |   "fromDate": "2025-01-01",
      |   "toDate": "2026-01-31",
      |   "foreignProperty": [
      |      {
      |         "countryCode": "FRA",
      |         "income":{
      |            "rentIncome":{
      |               "rentAmount": 1000.99
      |            },
      |            "foreignTaxCreditRelief": false,
      |            "premiumsOfLeaseGrant": 1000.99,
      |            "otherPropertyIncome": 2000.99,
      |            "foreignTaxPaidOrDeducted": 3000.99,
      |            "specialWithholdingTaxOrUkTaxPaid": 4000.99
      |         },
      |         "expenses": {
      |            "premisesRunningCosts": 1000.99,
      |            "repairsAndMaintenance": 2000.99,
      |            "financialCosts": 3000.99,
      |            "professionalFees": 4000.99,
      |            "costOfServices": 5000.99,
      |            "travelCosts": 6000.99,
      |            "residentialFinancialCost": 7000.99,
      |            "broughtFwdResidentialFinancialCost": 8000.99,
      |            "other": 9000.99
      |         }
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val consolidatedMtdRequestJson: JsValue = Json.parse(
    """{
      |   "fromDate": "2025-01-01",
      |   "toDate": "2026-01-31",
      |   "foreignProperty": [
      |      {
      |         "countryCode": "FRA",
      |         "income":{
      |            "rentIncome":{
      |               "rentAmount": 1000.99
      |            },
      |            "foreignTaxCreditRelief": false,
      |            "premiumsOfLeaseGrant": 1000.99,
      |            "otherPropertyIncome": 2000.99,
      |            "foreignTaxPaidOrDeducted": 3000.99,
      |            "specialWithholdingTaxOrUkTaxPaid": 4000.99
      |         },
      |         "expenses": {
      |            "consolidatedExpenses": 1000.99
      |         }
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val regularDownstreamRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2025-01-01",
      |  "toDate": "2026-01-31",
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 1000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 1000.99,
      |        "otherPropertyIncome": 2000.99,
      |        "foreignTaxPaidOrDeducted": 3000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 4000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 1000.99,
      |        "repairsAndMaintenance": 2000.99,
      |        "financialCostsAmount": 3000.99,
      |        "professionalFeesAmount": 4000.99,
      |        "costOfServicesAmount": 5000.99,
      |        "travelCostsAmount": 6000.99,
      |        "residentialFinancialCostAmount": 7000.99,
      |        "broughtFwdResidentialFinancialCostAmount": 8000.99,
      |        "otherAmount": 9000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val consolidatedDownstreamRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2025-01-01",
      |  "toDate": "2026-01-31",
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 1000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 1000.99,
      |        "otherPropertyIncome": 2000.99,
      |        "foreignTaxPaidOrDeducted": 3000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 4000.99
      |      },
      |      "expenses": {
      |        "consolidatedExpenseAmount": 1000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

}
