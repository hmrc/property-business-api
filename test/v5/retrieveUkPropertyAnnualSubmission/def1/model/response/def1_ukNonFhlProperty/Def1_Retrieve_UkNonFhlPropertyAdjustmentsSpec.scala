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

package v5.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty

import play.api.libs.json.Json
import support.UnitSpec

class Def1_Retrieve_UkNonFhlPropertyAdjustmentsSpec extends UnitSpec {

  val model: Def1_Retrieve_UkNonFhlPropertyAdjustments = Def1_Retrieve_UkNonFhlPropertyAdjustments(
    balancingCharge = Some(1.01),
    privateUseAdjustment = Some(2.01),
    businessPremisesRenovationAllowanceBalancingCharges = Some(3.01),
    nonResidentLandlord = true,
    rentARoom = Some(
      Def1_Retrieve_UkNonFhlPropertyRentARoom(
        jointlyLet = true
      ))
  )

  "reads" should {
    "read JSON into a model" when {
      "ukOtherRentARoom field name is used (API#1598)" in {
        Json
          .parse("""
            |{
            |   "balancingCharge":1.01,
            |   "privateUseAdjustment":2.01,
            |   "businessPremisesRenovationAllowanceBalancingCharges":3.01,
            |   "nonResidentLandlord":true,
            |   "ukOtherRentARoom":{
            |      "jointlyLet":true
            |   }
            |}
            |""".stripMargin)
          .as[Def1_Retrieve_UkNonFhlPropertyAdjustments] shouldBe model
      }

      "rentARoom field name is used (API#1805)" in {
        Json
          .parse("""
                   |{
                   |   "balancingCharge":1.01,
                   |   "privateUseAdjustment":2.01,
                   |   "businessPremisesRenovationAllowanceBalancingCharges":3.01,
                   |   "nonResidentLandlord":true,
                   |   "rentARoom":{
                   |      "jointlyLet":true
                   |   }
                   |}
                   |""".stripMargin)
          .as[Def1_Retrieve_UkNonFhlPropertyAdjustments] shouldBe model
      }

      "neither ukOtherRentARoom nor rentARoom are present" in {
        Json
          .parse("""
                   |{
                   |   "balancingCharge":1.01,
                   |   "privateUseAdjustment":2.01,
                   |   "businessPremisesRenovationAllowanceBalancingCharges":3.01,
                   |   "nonResidentLandlord":true
                   |}
                   |""".stripMargin)
          .as[Def1_Retrieve_UkNonFhlPropertyAdjustments] shouldBe model.copy(rentARoom = None)

      }
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe Json.parse("""
      |{
      |   "balancingCharge":1.01,
      |   "privateUseAdjustment":2.01,
      |   "businessPremisesRenovationAllowanceBalancingCharges":3.01,
      |   "nonResidentLandlord":true,
      |   "rentARoom":{
      |      "jointlyLet":true
      |   }
      |}
      |""".stripMargin)
    }
  }

}
