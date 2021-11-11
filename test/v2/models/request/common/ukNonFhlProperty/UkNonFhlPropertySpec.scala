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

package v2.models.request.common.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

class UkNonFhlPropertySpec extends UnitSpec {

  val requestBody: UkNonFhlProperty =
    UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
        Some(UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
      )),
      Some(UkNonFhlPropertyExpenses(
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(988.18)
      ))
    )


  val mtdJson: JsValue = Json.parse(
    """
      |{
      |    "income": {
      |        "premiumsOfLeaseGrant": 41.12,
      |        "reversePremiums": 84.31,
      |        "periodAmount": 9884.93,
      |        "taxDeducted": 842.99,
      |        "otherIncome": 31.44,
      |        "rentARoom": {
      |            "rentsReceived": 947.66
      |        }
      |    },
      |    "expenses": {
      |        "consolidatedExpense": 988.18
      |    }
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse(
    """
      |{
      |    "income": {
      |        "premiumsOfLeaseGrant": 41.12,
      |        "reversePremiums": 84.31,
      |        "periodAmount": 9884.93,
      |        "taxDeducted": 842.99,
      |        "otherIncome": 31.44,
      |        "ukOtherRentARoom": {
      |            "rentsReceived": 947.66
      |        }
      |    },
      |    "expenses": {
      |        "consolidatedExpenses": 988.18
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[UkNonFhlProperty] shouldBe requestBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe desJson
      }
    }
  }
}