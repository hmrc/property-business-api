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

package v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class UkNonFhlPropertyAdjustmentsSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "lossBroughtForward":334.45,
      |   "balancingCharge":565.34,
      |   "privateUseAdjustment":533.54,
      |   "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |   "nonResidentLandlord":true,
      |   "ukOtherRentARoom":{
      |      "jointlyLet":true
      |   }
      |}
      |""".stripMargin)

  val model: UkNonFhlPropertyAdjustments = UkNonFhlPropertyAdjustments(
    lossBroughtForward = Some(334.45),
    balancingCharge = Some(565.34),
    privateUseAdjustment = Some(533.54),
    businessPremisesRenovationAllowanceBalancingCharges = Some(563.34),
    nonResidentLandlord = true,
    rentARoom = Some(
      UkNonFhlPropertyRentARoom(
        jointlyLet = true
      ))
  )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "lossBroughtForward":334.45,
      |   "balancingCharge":565.34,
      |   "privateUseAdjustment":533.54,
      |   "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |   "nonResidentLandlord":true,
      |   "rentARoom":{
      |      "jointlyLet":true
      |   }
      |}
      |""".stripMargin)

  "reads" should {
    "read JSON into a model" in {
      downstreamJson.as[UkNonFhlPropertyAdjustments] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }
}
