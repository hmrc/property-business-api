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

package v2.models.response.retrieveUkPropertyPeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class FhlPropertyExpensesSpec extends UnitSpec {
  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "premisesRunningCosts": 0,
      |  "repairsAndMaintenance": 0,
      |  "financialCosts": 0,
      |  "professionalFees": 0,
      |  "costOfServices": 0,
      |  "other": 0,
      |  "consolidatedExpenses": 0,
      |  "travelCosts": 0,
      |  "ukFhlRentARoom": {
      |    "amountClaimed": 0
      |  }
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "premisesRunningCosts": 0,
      |  "repairsAndMaintenance": 0,
      |  "financialCosts": 0,
      |  "professionalFees": 0,
      |  "costOfServices": 0,
      |  "other": 0,
      |  "consolidatedExpenses": 0,
      |  "travelCosts": 0,
      |  "rentARoom": {
      |    "amountClaimed": 0
      |  }
      |}
    """.stripMargin
  )

  val model: FhlPropertyExpenses = FhlPropertyExpenses(Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(RentARoomExpenses(Some(0))), Some(0))

  "FhlPropertyExpenses" when {
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[FhlPropertyExpenses] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}
