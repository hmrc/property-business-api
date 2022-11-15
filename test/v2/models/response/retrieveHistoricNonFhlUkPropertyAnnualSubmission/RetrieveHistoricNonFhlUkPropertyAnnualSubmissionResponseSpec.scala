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

package v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import mocks.MockAppConfig
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse._

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments":
      |   {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 300.00,
      |      "privateUseAdjustment": 400.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
      |      "nonResidentLandlord": true,
      |      "ukRentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "otherCapitalAllowance": 300.00,
      |      "zeroEmissionGoodsVehicleAllowance": 400.00,
      |      "businessPremisesRenovationAllowance": 200.00,
      |      "costOfReplacingDomGoods": 200.00,
      |      "propertyIncomeAllowance": 30.02
      |   }
      |}
      |""".stripMargin)

  val mtdJson: JsValue                                   = Json.parse("""
       |{
       |   "annualAdjustments":
       |   {
       |      "lossBroughtForward": 200.00,
       |      "balancingCharge": 300.00,
       |      "privateUseAdjustment": 400.00,
       |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
       |      "nonResidentLandlord": true,
       |      "rentARoom": {
       |         "jointlyLet": true
       |      }
       |   },
       |   "annualAllowances": {
       |      "annualInvestmentAllowance": 200.00,
       |      "otherCapitalAllowance": 300.00,
       |      "zeroEmissionGoodsVehicleAllowance": 400.00,
       |      "businessPremisesRenovationAllowance": 200.00,
       |      "costOfReplacingDomesticGoods": 200.00,
       |      "propertyIncomeAllowance": 30.02
       |   }
       |}
       |""".stripMargin)
  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val model: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse = RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(
    Some(
      AnnualAdjustments(
        lossBroughtForward = decimal("200.00"),
        balancingCharge = decimal("300.00"),
        privateUseAdjustment = decimal("400.00"),
        businessPremisesRenovationAllowanceBalancingCharges = decimal("80.02"),
        nonResidentLandlord = true,
        rentARoom = Option(RentARoom(jointlyLet = true))
      )),
    Some(
      AnnualAllowances(
        annualInvestmentAllowance = decimal("200.00"),
        otherCapitalAllowance = decimal("300.00"),
        zeroEmissionGoodsVehicleAllowance = decimal("400.00"),
        businessPremisesRenovationAllowance = decimal("200.00"),
        costOfReplacingDomesticGoods = decimal("200.00"),
        propertyIncomeAllowance = decimal("30.02")
      )
    )
  )

  "reads" should {
    "return a correct object" when {
      "a correct Json is passed" in {
        val result = downstreamJson.as[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]
        result shouldBe model
      }
    }
  }

  "writes" should {
    "return a correct Json" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }
}
