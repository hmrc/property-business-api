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

package v5.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class HistoricFhlAnnualAdjustmentsSpec extends UnitSpec {

  val requestBody: HistoricFhlAnnualAdjustments =
    HistoricFhlAnnualAdjustments(
      Some(200.00),
      Some(200.00),
      Some(200.00),
      periodOfGraceAdjustment = true,
      Some(200.00),
      nonResidentLandlord = true,
      Some(UkPropertyAdjustmentsRentARoom(true))
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |   "lossBroughtForward": 200.00,
      |   "balancingCharge": 200.00,
      |   "privateUseAdjustment": 200.00,
      |   "periodOfGraceAdjustment": true,
      |   "businessPremisesRenovationAllowanceBalancingCharges": 200.00,
      |   "nonResidentLandlord": true,
      |   "rentARoom": {
      |     "jointlyLet": true
      |    }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |   "lossBroughtForward": 200.00,
      |   "balancingCharge": 200.00,
      |   "privateUseAdjustment": 200.00,
      |   "periodOfGraceAdjustment": true,
      |   "businessPremisesRenovationAllowanceBalancingCharges": 200.00,
      |   "nonResidentLandlord": true,
      |   "ukRentARoom": {
      |     "jointlyLet": true
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[HistoricFhlAnnualAdjustments] shouldBe requestBody
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
