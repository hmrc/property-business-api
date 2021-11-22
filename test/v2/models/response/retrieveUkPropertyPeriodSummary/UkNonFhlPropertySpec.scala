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

package v2.models.response.retrieveUkPropertyPeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class UkNonFhlPropertySpec extends UnitSpec {
  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "income": {
      |    "premiumsOfLeaseGrant": 0,
      |    "reversePremiums": 0,
      |    "periodAmount": 0,
      |    "taxDeducted": 0,
      |    "otherIncome": 0,
      |    "ukOtherRentARoom": {
      |      "rentsReceived": 0
      |    }
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 0,
      |    "repairsAndMaintenance": 0,
      |    "financialCosts": 0,
      |    "professionalFees": 0,
      |    "costOfServices": 0,
      |    "other": 0,
      |    "consolidatedExpenses": 0,
      |    "residentialFinancialCost": 0,
      |    "travelCosts": 0,
      |    "residentialFinancialCostsCarriedForward": 0,
      |    "ukOtherRentARoom": {
      |      "amountClaimed": 0
      |    }
      |  }
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "income": {
      |    "premiumsOfLeaseGrant": 0,
      |    "reversePremiums": 0,
      |    "periodAmount": 0,
      |    "taxDeducted": 0,
      |    "otherIncome": 0,
      |    "rentARoom": {
      |      "rentsReceived": 0
      |    }
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 0,
      |    "repairsAndMaintenance": 0,
      |    "financialCosts": 0,
      |    "professionalFees": 0,
      |    "costOfServices": 0,
      |    "other": 0,
      |    "consolidatedExpenses": 0,
      |    "residentialFinancialCost": 0,
      |    "travelCosts": 0,
      |    "residentialFinancialCostsCarriedForward": 0,
      |    "rentARoom": {
      |      "amountClaimed": 0
      |    }
      |  }
      |}
    """.stripMargin
  )

  val model: UkNonFhlProperty = UkNonFhlProperty(Some(NonFhlPropertyIncome(Some(0), Some(0), Some(0), Some(0), Some(0), Some(RentARoomIncome(Some(0))))), Some(NonFhlPropertyExpenses(Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(0), Some(RentARoomExpenses(Some(0))), Some(0))))

  "NonUkFhlProperty" when {
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[UkNonFhlProperty] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}
