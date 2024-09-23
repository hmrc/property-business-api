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

package v5.retrieveUkPropertyAnnualSubmission.def2.response.ukProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.ukProperty._

class RetrieveUkPropertyAdjustmentsSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "balancingCharge":565.34,
      |   "privateUseAdjustment":533.54,
      |   "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |   "nonResidentLandlord":true,
      |   "rentARoom":{
      |      "jointlyLet":true
      |   }
      |}
      |""".stripMargin)

  val model: RetrieveUkPropertyAdjustments = RetrieveUkPropertyAdjustments(
    balancingCharge = Some(565.34),
    privateUseAdjustment = Some(533.54),
    businessPremisesRenovationAllowanceBalancingCharges = Some(563.34),
    nonResidentLandlord = true,
    rentARoom = Some(
      RetrieveUkPropertyRentARoom(
        jointlyLet = true
      ))
  )

  val mtdJson: JsValue = Json.parse("""
      |{
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
      downstreamJson.as[RetrieveUkPropertyAdjustments] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
