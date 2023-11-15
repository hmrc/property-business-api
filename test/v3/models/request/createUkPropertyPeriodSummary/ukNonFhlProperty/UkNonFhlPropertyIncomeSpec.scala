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

package v3.models.request.createUkPropertyPeriodSummary.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v3.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

class UkNonFhlPropertyIncomeSpec extends UnitSpec {

  val requestBody: UkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(
      Some(41.12),
      Some(84.31),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(
        UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
    )

  val mtdJson: JsValue = Json.parse("""
                                      |{
                                      |    "premiumsOfLeaseGrant": 41.12,
                                      |    "reversePremiums": 84.31,
                                      |    "periodAmount": 9884.93,
                                      |    "taxDeducted": 842.99,
                                      |    "otherIncome": 31.44,
                                      |    "rentARoom": {
                                      |        "rentsReceived": 947.66
                                      |    }
                                      |}
                                      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
                                      |{
                                      |    "premiumsOfLeaseGrant": 41.12,
                                      |    "reversePremiums": 84.31,
                                      |    "periodAmount": 9884.93,
                                      |    "taxDeducted": 842.99,
                                      |    "otherIncome": 31.44,
                                      |    "ukOtherRentARoom": {
                                      |        "rentsReceived": 947.66
                                      |    }
                                      |}
                                      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[UkNonFhlPropertyIncome] shouldBe requestBody
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
