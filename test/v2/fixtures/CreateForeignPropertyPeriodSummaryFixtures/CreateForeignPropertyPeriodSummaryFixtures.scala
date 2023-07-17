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

package fixtures.CreateForeignPropertyPeriodSummaryFixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequestBody

trait CreateForeignPropertyPeriodSummaryFixtures {

  private val regularForeignFhlEea = Some(
    CreateForeignFhlEea(
      income = Some(ForeignFhlEeaIncome(Some(1000.99))),
      expenses = Some(CreateForeignFhlEeaExpenses(
        premisesRunningCosts = Some(1000.99),
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
    CreateForeignFhlEea(
      income = Some(ForeignFhlEeaIncome(Some(1000.99))),
      expenses = Some(CreateForeignFhlEeaExpenses(
        premisesRunningCosts = None,
        repairsAndMaintenance = None,
        financialCosts = None,
        professionalFees = None,
        costOfServices = None,
        travelCosts = None,
        other = None,
        consolidatedExpenses = Some(1000.99)
      ))
    ))

  private val foreignNonFhlPropertyIncome = Some(
    ForeignNonFhlPropertyIncome(
      rentIncome = Some(ForeignNonFhlPropertyRentIncome(Some(1000.99))),
      foreignTaxCreditRelief = false,
      premiumsOfLeaseGrant = Some(1000.99),
      otherPropertyIncome = Some(2000.99),
      foreignTaxPaidOrDeducted = Some(3000.99),
      specialWithholdingTaxOrUkTaxPaid = Some(4000.99)
    ))

  private val regularForeignNonFhlPropertyExpenses = Some(
    CreateForeignNonFhlPropertyExpenses(
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

  private val consolidatedForeignNonFhlPropertyExpenses = Some(
    CreateForeignNonFhlPropertyExpenses(
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
    Seq(CreateForeignNonFhlPropertyEntry(countryCode = "FRA", income = foreignNonFhlPropertyIncome, expenses = regularForeignNonFhlPropertyExpenses)))

  private val consolidatedForeignNonFhlProperty = Some(
    Seq(
      CreateForeignNonFhlPropertyEntry(
        countryCode = "FRA",
        income = foreignNonFhlPropertyIncome,
        expenses = consolidatedForeignNonFhlPropertyExpenses
      )))

  val regularExpensesRequestBody: CreateForeignPropertyPeriodSummaryRequestBody = CreateForeignPropertyPeriodSummaryRequestBody(
    fromDate = "2020-01-01",
    toDate = "2020-01-31",
    foreignFhlEea = regularForeignFhlEea,
    foreignNonFhlProperty = regularForeignNonFhlProperty
  )

  val consolidatedExpensesRequestBody: CreateForeignPropertyPeriodSummaryRequestBody = CreateForeignPropertyPeriodSummaryRequestBody(
    fromDate = "2020-01-01",
    toDate = "2020-01-31",
    foreignFhlEea = consolidatedForeignFhlEea,
    foreignNonFhlProperty = consolidatedForeignNonFhlProperty
  )

  val regularMtdRequestJson: JsValue = Json.parse(
    """{
      |   "fromDate": "2020-01-01",
      |   "toDate": "2020-01-31",
      |   "foreignFhlEea": {
      |      "income": {
      |         "rentAmount": 1000.99
      |      },
      |      "expenses": {
      |         "premisesRunningCosts": 1000.99,
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
      |   "fromDate": "2020-01-01",
      |   "toDate": "2020-01-31",
      |   "foreignFhlEea": {
      |      "income": {
      |         "rentAmount": 1000.99
      |      },
      |      "expenses": {
      |         "consolidatedExpenses": 1000.99
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
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
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
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
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
