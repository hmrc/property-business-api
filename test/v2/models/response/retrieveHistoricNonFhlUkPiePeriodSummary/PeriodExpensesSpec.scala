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

package v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class PeriodExpensesSpec extends UnitSpec with JsonErrorValidators {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val periodExpenses: PeriodExpenses =
    PeriodExpenses(
      decimal("5000.99"),
      decimal("4999.99"),
      decimal("4998.99"),
      decimal("4997.99"),
      decimal("4996.99"),
      decimal("4995.99"),
      decimal("4994.99"),
      decimal("4993.99"),
      decimal("4992.99"),
      decimal("4991.99"),
      Option(RentARoomExpenses(Some(4990.99)))
    )

  val writesJson: JsValue = Json.parse(
    """{
      |    "premisesRunningCosts": 5000.99,
      |    "repairsAndMaintenance": 4999.99,
      |    "financialCosts": 4998.99,
      |    "professionalFees": 4997.99,
      |    "costOfServices": 4996.99,
      |    "other": 4995.99,
      |    "travelCosts": 4994.99,
      |    "residentialFinancialCost": 4993.99,
      |    "residentialFinancialCostsCarriedForward": 4992.99,
      |    "consolidatedExpenses": 4991.99,
      |    "rentARoom":{
      |      "amountClaimed":4990.99
      |    }
      |  }
      |""".stripMargin
  )

  val readsJson: JsValue = Json.parse("""{
            |         "premisesRunningCosts": 5000.99,
            |         "repairsAndMaintenance": 4999.99,
            |         "financialCosts": 4998.99,
            |         "professionalFees": 4997.99,
            |         "costOfServices": 4996.99,
            |         "other": 4995.99,
            |         "travelCosts": 4994.99,
            |         "residentialFinancialCost": 4993.99,
            |         "residentialFinancialCostsCarriedForward": 4992.99,         
            |         "consolidatedExpenses": 4991.99,
            |         "ukRentARoom": {
            |            "amountClaimed": 4990.99
            |         }
            |      }
            |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[PeriodExpenses] shouldBe periodExpenses
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(periodExpenses) shouldBe writesJson
      }
    }
  }
}
