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

package v1.models.request.amendForeignProperty.foreignFhlEea

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class ForeignFhlEeaSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "income": {
       |    "rentAmount": 567.83,
       |    "taxDeducted": 4321.92
       |    },
       |  "expenditure": {
       |    "premisesRunningCosts": 4567.98,
       |    "repairsAndMaintenance": 98765.67,
       |    "financialCosts": 4566.95,
       |    "professionalFees": 23.65,
       |    "costsOfServices": 4567.77,
       |    "travelCosts": 456.77,
       |    "other": 567.67,
       |    "consolidatedExpenses": 456.98
       |  }
       |}
       |""".stripMargin)

  val model: ForeignFhlEea = ForeignFhlEea(
    income = Income(rentAmount = 567.83, taxDeducted = Some(4321.92)),
    expenditure = Some(Expenditure(
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

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |  "income": {
       |    "rentAmount": 567.83,
       |    "taxDeducted": 4321.92
       |    },
       |  "expenses": {
       |    "premisesRunningCostsAmount": 4567.98,
       |    "repairsAndMaintenanceAmount": 98765.67,
       |    "financialCostsAmount": 4566.95,
       |    "professionalFeesAmount": 23.65,
       |    "costsOfServicesAmount": 4567.77,
       |    "travelCostsAmount": 456.77,
       |    "otherAmount": 567.67,
       |    "consolidatedExpensesAmount": 456.98
       |  }
       |}
       |""".stripMargin)


  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[ForeignFhlEea] shouldBe model
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
