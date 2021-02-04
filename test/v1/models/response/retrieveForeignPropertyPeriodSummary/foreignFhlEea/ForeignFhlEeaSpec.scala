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

package v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignFhlEeaSpec extends UnitSpec with JsonErrorValidators {

  val foreignFhlEea = ForeignFhlEea(
    ForeignFhlEeaIncome(5000.99, Some(5000.99)),
    Some(ForeignFhlEeaExpenditure(
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
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  }""".stripMargin)

  val readsJson = Json.parse(
    """{
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCostsAmount": 5000.99,
      |      "repairsAndMaintenanceAmount": 5000.99,
      |      "financialCostsAmount": 5000.99,
      |      "professionalFeesAmount": 5000.99,
      |      "costOfServicesAmount": 5000.99,
      |      "travelCostsAmount": 5000.99,
      |      "otherAmount": 5000.99,
      |      "consolidatedExpensesAmount": 5000.99
      |    }
      |  }""".stripMargin)


  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[ForeignFhlEea] shouldBe foreignFhlEea
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignFhlEea) shouldBe writesJson
      }
    }
  }
}
