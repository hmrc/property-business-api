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

package v1.models.response.foreignPropertyPeriodSummary.retrieveForeignPropertyPeriodSummary.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertyExpenditureSpec extends UnitSpec with JsonErrorValidators {

  val foreignPropertyExpenditure = ForeignPropertyExpenditure(
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
  )

  val writesJson = Json.parse(
    """{
      |  "premisesRunningCosts": 5000.99,
      |  "repairsAndMaintenance": 5000.99,
      |  "financialCosts": 5000.99,
      |  "professionalFees": 5000.99,
      |  "costsOfServices": 5000.99,
      |  "travelCosts": 5000.99,
      |  "residentialFinancialCost": 5000.99,
      |  "broughtFwdResidentialFinancialCost": 5000.99,
      |  "other": 5000.99,
      |  "consolidatedExpenses": 5000.99
      |}""".stripMargin)

  val readsJson = Json.parse(
    """{
      |  "premisesRunningCostsAmount": 5000.99,
      |  "repairsAndMaintenanceAmount": 5000.99,
      |  "financialCostsAmount": 5000.99,
      |  "professionalFeesAmount": 5000.99,
      |  "costOfServicesAmount": 5000.99,
      |  "travelCostsAmount": 5000.99,
      |  "residentialFinancialCostAmount": 5000.99,
      |  "broughtFwdResidentialFinancialCostAmount": 5000.99,
      |  "otherAmount": 5000.99,
      |  "consolidatedExpensesAmount": 5000.99
      |}""".stripMargin)


  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[ForeignPropertyExpenditure] shouldBe foreignPropertyExpenditure
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignPropertyExpenditure) shouldBe writesJson
      }
    }
  }
}
