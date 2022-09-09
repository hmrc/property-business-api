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

package v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBodySpec extends UnitSpec {

  val requestBody: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
    CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(
      annualAdjustments = Some(
        HistoricNonFhlAnnualAdjustments(
          lossBroughtForward = Some(200.00),
          privateUseAdjustment = Some(200.00),
          balancingCharge = Some(200.00),
          businessPremisesRenovationAllowanceBalancingCharges = Some(80.02),
          nonResidentLandlord = true,
          rentARoom = Some(UkPropertyAdjustmentsRentARoom(true))
        )
      ),
      annualAllowances = Some(
        HistoricNonFhlAnnualAllowances(
          annualInvestmentAllowance = Some(200.00),
          otherCapitalAllowance = Some(200.00),
          zeroEmissionGoodsVehicleAllowance = Some(200.00),
          businessPremisesRenovationAllowance = Some(200.00),
          costOfReplacingDomesticGoods = Some(200.00),
          propertyIncomeAllowance = Some(30.02)
        )
      )
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "otherCapitalAllowance": 200.00,
      |      "zeroEmissionGoodsVehicleAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 200.00,
      |      "costOfReplacingDomesticGoods": 200.00,
      |      "propertyIncomeAllowance": 30.02
      |   }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
      |      "nonResidentLandlord": true,
      |      "ukRentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "otherCapitalAllowance": 200.00,
      |      "zeroEmissionGoodsVehicleAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 200.00,
      |      "costOfReplacingDomGoods": 200.00,
      |      "propertyIncomeAllowance": 30.02
      |   }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody] shouldBe requestBody
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
