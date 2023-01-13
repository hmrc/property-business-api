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

package v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class HistoricNonFhlAnnualAdjustmentsSpec extends UnitSpec {

  val annualAdjustments: HistoricNonFhlAnnualAdjustments =
    HistoricNonFhlAnnualAdjustments(
      lossBroughtForward = Some(100.00),
      privateUseAdjustment = Some(200.00),
      balancingCharge = Some(300.00),
      businessPremisesRenovationAllowanceBalancingCharges = Some(400.00),
      nonResidentLandlord = true,
      rentARoom = Some(UkPropertyAdjustmentsRentARoom(jointlyLet = true))
    )

  val validMtdJson: JsValue = Json.parse(
    """
      |{
      |      "lossBroughtForward": 100.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 300.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |}
      |""".stripMargin
  )

  val validDownstreamJson: JsValue = Json.parse(
    """
      |{
      |      "lossBroughtForward": 100.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 300.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
      |      "nonResidentLandlord": true,
      |      "ukRentARoom": {
      |         "jointlyLet": true
      |      }
      |}
      |""".stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[HistoricNonFhlAnnualAdjustments] shouldBe annualAdjustments
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(annualAdjustments) shouldBe validDownstreamJson
      }
    }
  }
}
