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

package v6.createForeignPropertyPeriodSummary.def2.model

import play.api.libs.json.{JsValue, Json}
import v6.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignFhlEea._
import v6.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignPropertyEntry._
import v6.createForeignPropertyPeriodSummary.model.request.Def2_CreateForeignPropertyPeriodSummaryRequestBody

trait Def2_CreateForeignPropertyPeriodSummaryFixtures {

  private val regularForeignFhlEea = Some(
    Def2_Create_CreateForeignFhlEea(
      income = Some(Def2_Create_ForeignFhlEeaIncome(Some(1000.99))),
      expenses = Some(Def2_Create_CreateForeignFhlEeaExpenses(
        premisesRunningCosts = Some(-1000.99),
        repairsAndMaintenance = Some(2000.99),
        financialCosts = Some(3000.99),
        professionalFees = Some(4000.99),
        costOfServices = Some(5000.99),
        travelCosts = Some(6000.99),
        other = Some(7000.99),
        consolidatedExpenses = None
      ))
    ))

  private val consolidatedForeignFhlEea = Some(
    Def2_Create_CreateForeignFhlEea(
      income = Some(Def2_Create_ForeignFhlEeaIncome(Some(1000.99))),
      expenses = Some(Def2_Create_CreateForeignFhlEeaExpenses(
        premisesRunningCosts = None,
        repairsAndMaintenance = None,
        financialCosts = None,
        professionalFees = None,
        costOfServices = None,
        travelCosts = None,
        other = None,
        consolidatedExpenses = Some(-1000.99)
      ))
    ))

  private val foreignNonFhlPropertyIncome = Some(
    Def2_Create_ForeignNonFhlPropertyIncome(
      rentIncome = Some(Def2_Create_ForeignNonFhlPropertyRentIncome(Some(1000.99))),
      foreignTaxCreditRelief = false,
      premiumsOfLeaseGrant = Some(1000.99),
      otherPropertyIncome = Some(2000.99),
      foreignTaxPaidOrDeducted = Some(3000.99),
      specialWithholdingTaxOrUkTaxPaid = Some(4000.99)
    ))

  private val regularForeignNonFhlPropertyExpenses = Some(
    Def2_Create_CreateForeignNonFhlPropertyExpenses(
      premisesRunningCosts = Some(1000.99),
      repairsAndMaintenance = Some(2000.99),
      financialCosts = Some(3000.99),
      professionalFees = Some(4000.99),
      costOfServices = Some(-5000.99),
      travelCosts = Some(6000.99),
      residentialFinancialCost = Some(7000.99),
      broughtFwdResidentialFinancialCost = Some(8000.99),
      other = Some(9000.99),
      consolidatedExpenses = None
    ))

  private val consolidatedForeignNonFhlPropertyExpenses = Some(
    Def2_Create_CreateForeignNonFhlPropertyExpenses(
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

  private val regularForeignNonFhlProperty = Some(
    List(
      Def2_Create_CreateForeignNonFhlPropertyEntry(
        countryCode = "FRA",
        income = foreignNonFhlPropertyIncome,
        expenses = regularForeignNonFhlPropertyExpenses)))

  private val consolidatedForeignNonFhlProperty = Some(
    List(
      Def2_Create_CreateForeignNonFhlPropertyEntry(
        countryCode = "FRA",
        income = foreignNonFhlPropertyIncome,
        expenses = consolidatedForeignNonFhlPropertyExpenses
      )))

  val regularExpensesRequestBody: Def2_CreateForeignPropertyPeriodSummaryRequestBody = Def2_CreateForeignPropertyPeriodSummaryRequestBody(
    fromDate = "2024-04-06",
    toDate = "2024-07-05",
    foreignFhlEea = regularForeignFhlEea,
    foreignNonFhlProperty = regularForeignNonFhlProperty
  )

  val consolidatedExpensesRequestBody: Def2_CreateForeignPropertyPeriodSummaryRequestBody = Def2_CreateForeignPropertyPeriodSummaryRequestBody(
    fromDate = "2024-07-06",
    toDate = "2024-10-05",
    foreignFhlEea = consolidatedForeignFhlEea,
    foreignNonFhlProperty = consolidatedForeignNonFhlProperty
  )

  val regularMtdRequestJson: JsValue = Json.parse(
    """{
      |   "fromDate": "2024-04-06",
      |   "toDate": "2024-07-05",
      |   "foreignFhlEea": {
      |      "income": {
      |         "rentAmount": 1000.99
      |      },
      |      "expenses": {
      |         "premisesRunningCosts": -1000.99,
      |         "repairsAndMaintenance": 2000.99,
      |         "financialCosts": 3000.99,
      |         "professionalFees": 4000.99,
      |         "costOfServices": 5000.99,
      |         "travelCosts": 6000.99,
      |         "other": 7000.99
      |      }
      |   },
      |   "foreignNonFhlProperty": [
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
      |            "costOfServices": -5000.99,
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
      |   "fromDate": "2024-07-06",
      |   "toDate": "2024-10-05",
      |   "foreignFhlEea": {
      |      "income": {
      |         "rentAmount": 1000.99
      |      },
      |      "expenses": {
      |         "consolidatedExpenses": -1000.99
      |      }
      |   },
      |   "foreignNonFhlProperty": [
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
      |  "fromDate": "2024-04-06",
      |  "toDate": "2024-07-05",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 1000.99,
      |      "repairsAndMaintenance": 2000.99,
      |      "financialCosts": 3000.99,
      |      "professionalFees": 4000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 6000.99,
      |      "other": 7000.99
      |    }
      |  },
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
      |        "financialCosts": 3000.99,
      |        "professionalFees": 4000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 6000.99,
      |        "residentialFinancialCost": 7000.99,
      |        "broughtFwdResidentialFinancialCost": 8000.99,
      |        "other": 9000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val consolidatedDownstreamRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2024-07-06",
      |  "toDate": "2024-10-05",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1000.99
      |    },
      |    "expenses": {
      |      "consolidatedExpenseAmount": 1000.99
      |    }
      |  },
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
