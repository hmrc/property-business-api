/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.UkPropertyExpensesRentARoom

class UkNonFhlPropertyExpensesSpec extends UnitSpec {

  val requestBody: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(
      Some(567.53),
      Some(324.65),
      Some(453.56),
      Some(535.78),
      Some(678.34),
      Some(682.34),
      Some(1000.45),
      Some(645.56),
      Some(672.34),
      Some(
        UkPropertyExpensesRentARoom(
          Some(545.9)
        )),
      None
    )

  val mtdJson: JsValue = Json.parse("""
      |{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34, 
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |""".stripMargin)

  val backendJson: JsValue = Json.parse("""
      |{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "ukRentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[UkNonFhlPropertyExpenses] shouldBe requestBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe backendJson
      }
    }
  }
}
