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

package v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.Def1_foreignPropertyEntry

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class Def1_Create_CreateForeignPropertyExpensesSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "premisesRunningCosts": 5635.43,
      |  "repairsAndMaintenance": 3456.65,
      |  "financialCostsAmount": 34532.21,
      |  "professionalFeesAmount": 32465.32,
      |  "costOfServicesAmount": 2567.21,
      |  "travelCostsAmount": 2345.76,
      |  "residentialFinancialCostAmount": 21235.22,
      |  "broughtFwdResidentialFinancialCostAmount": 12556.00,
      |  "otherAmount": 2425.11,
      |  "consolidatedExpenseAmount": 352.66
      |}
    """.stripMargin
  )

  val model: Def1_Create_CreateForeignPropertyExpenses = Def1_Create_CreateForeignPropertyExpenses(
    premisesRunningCosts = Some(5635.43),
    repairsAndMaintenance = Some(3456.65),
    financialCostsAmount = Some(34532.21),
    professionalFeesAmount = Some(32465.32),
    costOfServicesAmount = Some(2567.21),
    travelCostsAmount = Some(2345.76),
    residentialFinancialCostAmount = Some(21235.22),
    broughtFwdResidentialFinancialCostAmount = Some(12556.00),
    otherAmount = Some(2425.11),
    consolidatedExpenseAmount = Some(352.66)
  )

  val ifsJson: JsValue = Json.parse(
    """
      |{
      |  "premisesRunningCosts": 5635.43,
      |  "repairsAndMaintenance": 3456.65,
      |  "financialCostsAmount": 34532.21,
      |  "professionalFeesAmount": 32465.32,
      |  "costOfServicesAmount": 2567.21,
      |  "travelCostsAmount": 2345.76,
      |  "residentialFinancialCostAmount": 21235.22,
      |  "broughtFwdResidentialFinancialCostAmount": 12556.00,
      |  "otherAmount": 2425.11,
      |  "consolidatedExpenseAmount": 352.66
      |}
    """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[Def1_Create_CreateForeignPropertyExpenses] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe ifsJson
      }
    }
  }

}
