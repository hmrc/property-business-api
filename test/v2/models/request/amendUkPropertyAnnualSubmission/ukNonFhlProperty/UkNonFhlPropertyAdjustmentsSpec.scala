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

package v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class UkNonFhlPropertyAdjustmentsSpec extends UnitSpec {

  val requestBody: UkNonFhlPropertyAdjustments =
    UkNonFhlPropertyAdjustments(
        Some(2000.20),
        Some(2000.30),
        Some(2000.40),
        true,
        Some(UkPropertyAdjustmentsRentARoom(true))
      )

  val validMtdJson: JsValue = Json.parse(
    """
      |{
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse(
    """
      |{
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "ukOtherRentARoom": {
      |        "jointlyLet": true
      |      }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[UkNonFhlPropertyAdjustments] shouldBe requestBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe validDownstreamJson
      }
    }
  }
}
