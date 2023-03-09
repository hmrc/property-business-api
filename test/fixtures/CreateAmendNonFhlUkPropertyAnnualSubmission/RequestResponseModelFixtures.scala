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

package fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.{ JsValue, Json }
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  HistoricNonFhlAnnualAdjustments,
  HistoricNonFhlAnnualAllowances
}

trait RequestResponseModelFixtures {

  val annualAdjustments: HistoricNonFhlAnnualAdjustments =
    HistoricNonFhlAnnualAdjustments(
      lossBroughtForward = Some(100.00),
      privateUseAdjustment = Some(200.00),
      balancingCharge = Some(300.00),
      businessPremisesRenovationAllowanceBalancingCharges = Some(400.00),
      nonResidentLandlord = true,
      rentARoom = Some(UkPropertyAdjustmentsRentARoom(true))
    )

  val annualAllowances: HistoricNonFhlAnnualAllowances =
    HistoricNonFhlAnnualAllowances(
      annualInvestmentAllowance = Some(500.00),
      zeroEmissionGoodsVehicleAllowance = Some(600.00),
      businessPremisesRenovationAllowance = Some(700.00),
      otherCapitalAllowance = Some(800.00),
      costOfReplacingDomesticGoods = Some(900.00),
      propertyIncomeAllowance = Some(1000.00)
    )

  val requestBody: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
    CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(
      Some(annualAdjustments),
      Some(annualAllowances)
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 100.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 300.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 500.00,
      |      "zeroEmissionGoodsVehicleAllowance": 600.00,
      |      "businessPremisesRenovationAllowance": 700.00,
      |      "otherCapitalAllowance": 800.00,
      |      "costOfReplacingDomesticGoods": 900.00,
      |      "propertyIncomeAllowance": 1000.00
      |   }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 100.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 300.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
      |      "nonResidentLandlord": true,
      |      "ukRentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 500.00,
      |      "zeroEmissionGoodsVehicleAllowance": 600.00,
      |      "businessPremisesRenovationAllowance": 700.00,
      |      "otherCapitalAllowance": 800.00,
      |      "costOfReplacingDomGoods": 900.00,
      |      "propertyIncomeAllowance": 1000.00
      |   }
      |}
      |""".stripMargin)
}