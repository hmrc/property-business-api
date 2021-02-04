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

package v1.models.request.common.foreignPropertyEntry

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class ExpenditureSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "premisesRunningCosts": 5635.43,
       |  "repairsAndMaintenance": 3456.65,
       |  "financialCosts": 34532.21,
       |  "professionalFees": 32465.32,
       |  "costsOfServices": 2567.21,
       |  "travelCosts": 2345.76,
       |  "residentialFinancialCost": 21235.22,
       |  "broughtFwdResidentialFinancialCost": 12556.00,
       |  "other": 2425.11,
       |  "consolidatedExpenses": 352.66
       |}
       |""".stripMargin)

  val model: ForeignPropertyExpenditure = ForeignPropertyExpenditure(
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
  )

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |  "premisesRunningCostsAmount": 5635.43,
       |  "repairsAndMaintenanceAmount": 3456.65,
       |  "financialCostsAmount": 34532.21,
       |  "professionalFeesAmount": 32465.32,
       |  "costsOfServicesAmount": 2567.21,
       |  "travelCostsAmount": 2345.76,
       |  "residentialFinancialCostAmount": 21235.22,
       |  "broughtFwdResidentialFinancialCostAmount": 12556.00,
       |  "otherAmount": 2425.11,
       |  "consolidatedExpensesAmount": 352.66
       |}
       |""".stripMargin)


  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[ForeignPropertyExpenditure] shouldBe model
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

  "isEmpty" should {
    "return true when no fields are provided" in {
      ForeignPropertyExpenditure(None, None, None, None, None, None, None, None, None, None).isEmpty shouldBe true
    }
  }
}
