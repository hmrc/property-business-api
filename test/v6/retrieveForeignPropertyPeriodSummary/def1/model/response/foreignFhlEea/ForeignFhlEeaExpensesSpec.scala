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

package v6.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignFhlEea

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignFhlEea.ForeignFhlEeaExpenses

class ForeignFhlEeaExpensesSpec extends UnitSpec {

  private val foreignFhlEeaExpenses = ForeignFhlEeaExpenses(
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99)
  )

  private val writesJson = Json.parse("""
      |{
      |  "premisesRunningCosts": 5000.99,
      |  "repairsAndMaintenance": 5000.99,
      |  "financialCosts": 5000.99,
      |  "professionalFees": 5000.99,
      |  "costOfServices": 5000.99,
      |  "travelCosts": 5000.99,
      |  "other": 5000.99,
      |  "consolidatedExpenses": 5000.99
      |}""".stripMargin)

  private val readsJson = Json.parse("""
      |{
      |  "premisesRunningCosts": 5000.99,
      |  "repairsAndMaintenance": 5000.99,
      |  "financialCosts": 5000.99,
      |  "professionalFees": 5000.99,
      |  "costOfServices": 5000.99,
      |  "travelCosts": 5000.99,
      |  "other": 5000.99,
      |  "consolidatedExpense": 5000.99
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[ForeignFhlEeaExpenses] shouldBe foreignFhlEeaExpenses
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignFhlEeaExpenses) shouldBe writesJson
      }
    }
  }

}
