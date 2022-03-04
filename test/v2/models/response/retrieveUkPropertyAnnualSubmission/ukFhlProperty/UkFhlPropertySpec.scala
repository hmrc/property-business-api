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

package v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class UkFhlPropertySpec extends UnitSpec {
  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "allowances":{
      |      "annualInvestmentAllowance":123.45,
      |      "businessPremisesRenovationAllowance":345.56,
      |      "otherCapitalAllowance":345.34,
      |      "propertyIncomeAllowance":453.45,
      |      "electricChargePointAllowance":453.34,
      |      "zeroEmissionsCarAllowance":123.12
      |   },
      |   "adjustments":{
      |      "privateUseAdjustment":454.45,
      |      "balancingCharge":231.45,
      |      "periodOfGraceAdjustment":true,
      |      "businessPremisesRenovationAllowanceBalancingCharges":567.67,
      |      "nonResidentLandlord":true,
      |      "ukFhlRentARoom":{
      |         "jointlyLet":true
      |      }
      |   }
      |}
      |""".stripMargin)

  val model: UkFhlProperty = UkFhlProperty(
    adjustments = Some(
      UkFhlPropertyAdjustments(
        privateUseAdjustment = Some(454.45),
        balancingCharge = Some(231.45),
        periodOfGraceAdjustment = true,
        businessPremisesRenovationAllowanceBalancingCharges = Some(567.67),
        nonResidentLandlord = true,
        rentARoom = Some(
          UkFhlPropertyRentARoom(
            jointlyLet = true
          )),
      )
    ),
    allowances = Some(
      UkFhlPropertyAllowances(
        Some(123.45),
        Some(345.56),
        Some(345.34),
        Some(453.45),
        Some(453.34),
        Some(123.12)
      )
    )
  )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "allowances":{
      |      "annualInvestmentAllowance":123.45,
      |      "businessPremisesRenovationAllowance":345.56,
      |      "otherCapitalAllowance":345.34,
      |      "propertyIncomeAllowance":453.45,
      |      "electricChargePointAllowance":453.34,
      |      "zeroEmissionsCarAllowance":123.12
      |   },
      |   "adjustments":{
      |      "privateUseAdjustment":454.45,
      |      "balancingCharge":231.45,
      |      "periodOfGraceAdjustment":true,
      |      "businessPremisesRenovationAllowanceBalancingCharges":567.67,
      |      "nonResidentLandlord":true,
      |      "rentARoom":{
      |         "jointlyLet":true
      |      }
      |   }
      |}
      |""".stripMargin)

  "reads" should {
    "read JSON into a model" in {
      downstreamJson.as[UkFhlProperty] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }
}
