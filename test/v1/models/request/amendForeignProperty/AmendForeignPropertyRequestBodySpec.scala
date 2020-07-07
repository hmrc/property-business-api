/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.request.amendForeignProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.amendForeignProperty.foreignFhlEea.{ForeignFhlEea, Expenditure => ForeignFhlEeaExpenditure, Income => ForeignFhlEeaIncome}
import v1.models.request.amendForeignProperty.foreignPropertyEntry.{ForeignPropertyEntry, RentIncome, Expenditure => ForeignPropertyExpenditure, Income => ForeignPropertyIncome}

class AmendForeignPropertyRequestBodySpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 567.83,
       |      "taxDeducted": 4321.92
       |      },
       |    "expenditure": {
       |      "premisesRunningCosts": 4567.98,
       |      "repairsAndMaintenance": 98765.67,
       |      "financialCosts": 4566.95,
       |      "professionalFees": 23.65,
       |      "costsOfServices": 4567.77,
       |      "travelCosts": 456.77,
       |      "other": 567.67,
       |      "consolidatedExpenses": 456.98
       |    }
       |
       |  },
       |  "foreignProperty": [{
       |      "countryCode": "zzz",
       |      "income": {
       |        "rentIncome": {
       |          "rentAmount": 34456.30,
       |          "taxDeducted": 6334.34
       |        },
       |        "foreignTaxCreditRelief": true,
       |        "premiumOfLeaseGrant": 2543.43,
       |        "otherPropertyIncome": 54325.30,
       |        "foreignTaxTakenOff": 6543.01,
       |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
       |      },
       |      "expenditure": {
       |        "premisesRunningCosts": 5635.43,
       |        "repairsAndMaintenance": 3456.65,
       |        "financialCosts": 34532.21,
       |        "professionalFees": 32465.32,
       |        "costsOfServices": 2567.21,
       |        "travelCosts": 2345.76,
       |        "residentialFinancialCost": 21235.22,
       |        "broughtFwdResidentialFinancialCost": 12556.00,
       |        "other": 2425.11,
       |        "consolidatedExpenses": 352.66
       |      }
       |    }
       |  ]
       |}
       |""".stripMargin)

  private val foreignFhlEea: ForeignFhlEea = ForeignFhlEea(
    income = ForeignFhlEeaIncome(rentAmount = 567.83, taxDeducted = Some(4321.92)),
    expenditure = Some(ForeignFhlEeaExpenditure(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(4566.95),
      professionalFees = Some(23.65),
      costsOfServices = Some(4567.77),
      travelCosts = Some(456.77),
      other = Some(567.67),
      consolidatedExpenses = Some(456.98)
    ))
  )

  private val foreignProperty: ForeignPropertyEntry = ForeignPropertyEntry(
    countryCode = "zzz",
    income = ForeignPropertyIncome(
      rentIncome = RentIncome(rentAmount = 34456.30, taxDeducted = 6334.34),
      foreignTaxCreditRelief = true,
      premiumOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxTakenOff = Some(6543.01),
      specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
    ),
    expenditure = Some(ForeignPropertyExpenditure(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costsOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = Some(352.66)
    ))
  )

  val model: AmendForeignPropertyRequestBody = AmendForeignPropertyRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignProperty = Some(Seq(foreignProperty))
  )

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 567.83,
       |      "taxDeducted": 4321.92
       |      },
       |    "expenses": {
       |      "premisesRunningCostsAmount": 4567.98,
       |      "repairsAndMaintenanceAmount": 98765.67,
       |      "financialCostsAmount": 4566.95,
       |      "professionalFeesAmount": 23.65,
       |      "costsOfServicesAmount": 4567.77,
       |      "travelCostsAmount": 456.77,
       |      "otherAmount": 567.67,
       |      "consolidatedExpensesAmount": 456.98
       |    }
       |
       |  },
       |  "foreignProperty": [{
       |      "countryCode": "zzz",
       |      "income": {
       |        "rentIncome": {
       |          "rentAmount": 34456.30,
       |          "taxDeducted": 6334.34
       |        },
       |        "foreignTaxCreditRelief": true,
       |        "premiumOfLeaseGrantAmount": 2543.43,
       |        "otherPropertyIncomeAmount": 54325.30,
       |        "foreignTaxPaidOrDeducted": 6543.01,
       |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
       |      },
       |      "expenses": {
       |        "premisesRunningCostsAmount": 5635.43,
       |        "repairsAndMaintenanceAmount": 3456.65,
       |        "financialCostsAmount": 34532.21,
       |        "professionalFeesAmount": 32465.32,
       |        "costsOfServicesAmount": 2567.21,
       |        "travelCostsAmount": 2345.76,
       |        "residentialFinancialCostAmount": 21235.22,
       |        "broughtFwdResidentialFinancialCostAmount": 12556.00,
       |        "otherAmount": 2425.11,
       |        "consolidatedExpensesAmount": 352.66
       |      }
       |    }
       |  ]
       |}
       |""".stripMargin)


  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[AmendForeignPropertyRequestBody] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe desJson
      }
    }
  }
}
