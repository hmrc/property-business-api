/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertySpec extends UnitSpec with JsonErrorValidators {

  val foreignProperty = ForeignProperty(
    "FRA",
    ForeignPropertyIncome(
      ForeignPropertyRentIncome(5000.99),
      false,
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ),
    Some(ForeignPropertyExpenditure(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ))
  )

  val writesJson = Json.parse(
    """{
      |  "countryCode": "FRA",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 5000.99
      |    },
      |    "foreignTaxCreditRelief": false,
      |    "premiumOfLeaseGrant": 5000.99,
      |    "otherPropertyIncome": 5000.99,
      |    "foreignTaxTakenOff": 5000.99,
      |    "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |  },
      |  "expenditure": {
      |    "premisesRunningCosts": 5000.99,
      |    "repairsAndMaintenance": 5000.99,
      |    "financialCosts": 5000.99,
      |    "professionalFees": 5000.99,
      |    "costsOfServices": 5000.99,
      |    "travelCosts": 5000.99,
      |    "residentialFinancialCost": 5000.99,
      |    "broughtFwdResidentialFinancialCost": 5000.99,
      |    "other": 5000.99,
      |    "consolidatedExpenses": 5000.99
      |  }
      |}""".stripMargin)

  val readsJson = Json.parse(
    """{
      |  "countryCode": "FRA",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 5000.99
      |    },
      |    "foreignTaxCreditRelief": false,
      |    "premiumsOfLeaseGrant": 5000.99,
      |    "otherPropertyIncome": 5000.99,
      |    "foreignTaxPaidOrDeducted": 5000.99,
      |    "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 5000.99,
      |    "repairsAndMaintenance": 5000.99,
      |    "financialCosts": 5000.99,
      |    "professionalFees": 5000.99,
      |    "costOfServices": 5000.99,
      |    "travelCosts": 5000.99,
      |    "residentialFinancialCost": 5000.99,
      |    "broughtFwdResidentialFinancialCost": 5000.99,
      |    "other": 5000.99,
      |    "consolidatedExpense": 5000.99
      |  }
      |}""".stripMargin)


  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[ForeignProperty] shouldBe foreignProperty
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignProperty) shouldBe writesJson
      }
    }
  }
}
