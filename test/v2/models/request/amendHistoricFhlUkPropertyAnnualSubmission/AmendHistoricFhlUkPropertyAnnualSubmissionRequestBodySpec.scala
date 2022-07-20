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

package v2.models.request.amendHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.amendHistoricFhlUkPropertyAnnualSubmission.historicFhl._
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class AmendHistoricFhlUkPropertyAnnualSubmissionRequestBodySpec extends UnitSpec {

  val requestBody: AmendHistoricFhlUkPropertyAnnualSubmissionRequestBody =
    AmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(
      Some(
        HistoricFhlAnnualAdjustments(
        Some(200.00),
        Some(200.00),
        Some(200.00),
        periodOfGraceAdjustment = true,
        Some(200.00),
        nonResidentLandlord = true,
        Some(UkPropertyAdjustmentsRentARoom(true))
        )
      ),
      Some(HistoricFhlAnnualAllowances(
        Some(200.00),
        Some(100.00),
        Some(200.00),
        Some(20.00))
      )
    )

  val validMtdJson: JsValue = Json.parse(
    """
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 200.00,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 100.00,
      |      "otherCapitalAllowance": 200.00,
      |      "propertyIncomeAllowance": 20.00
      |   }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse(
    """
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 200.00,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 100.00,
      |      "otherCapitalAllowance": 200.00,
      |      "propertyIncomeAllowance": 20.00
      |   }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[AmendHistoricFhlUkPropertyAnnualSubmissionRequestBody] shouldBe requestBody
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
