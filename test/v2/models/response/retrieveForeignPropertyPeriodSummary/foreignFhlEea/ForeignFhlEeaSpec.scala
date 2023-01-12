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

package v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class ForeignFhlEeaSpec extends UnitSpec with JsonErrorValidators {

  val foreignFhlEea = ForeignFhlEea(
    Some(ForeignFhlEeaIncome(Some(5000.99))),
    Some(
      ForeignFhlEeaExpenses(
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

  val writesJson = Json.parse("""{
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  }""".stripMargin)

  val readsJson = Json.parse("""{
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpense": 5000.99
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
