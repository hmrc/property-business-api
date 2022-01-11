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

package v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.common.{Building, FirstYear, StructuredBuildingAllowance}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class UkNonFhlPropertySpec extends UnitSpec {

  val requestBody: UkNonFhlProperty =
      UkNonFhlProperty(
        Some(UkNonFhlPropertyAdjustments(
          Some(2000.10),
          Some(2000.20),
          Some(2000.30),
          Some(2000.40),
          true,
          Some(UkPropertyAdjustmentsRentARoom(true))
        )),
        Some(UkNonFhlPropertyAllowances(
          Some(2000.50),
          Some(2000.60),
          Some(2000.70),
          Some(2000.80),
          Some(2000.90),
          Some(3000.10),
          Some(3000.20),
          None,
          Some(Seq(StructuredBuildingAllowance(
            3000.30,
            Some(FirstYear(
              "2020-01-01",
              3000.40
            )),
            Building(
              Some("house name"),
              None,
              "GF49JH"
            )
          ))),
          Some(Seq(StructuredBuildingAllowance(
            3000.50,
            Some(FirstYear(
              "2020-01-01",
              3000.60
            )),
            Building(
              None,
              Some("house number"),
              "GF49JH"
            )
          )))
        ))
      )

  val validMtdJson: JsValue = Json.parse(
    """
      |{
      |    "allowances": {
      |      "annualInvestmentAllowance": 2000.50,
      |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |      "businessPremisesRenovationAllowance": 2000.70,
      |      "otherCapitalAllowance": 2000.80,
      |      "costOfReplacingDomesticGoods": 2000.90,
      |      "electricChargePointAllowance": 3000.10,
      |      "structuredBuildingAllowance": [
      |        {
      |          "amount": 3000.30,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.40
      |          },
      |          "building": {
      |            "name": "house name",
      |            "postcode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "enhancedStructuredBuildingAllowance": [
      |        {
      |          "amount": 3000.50,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.60
      |          },
      |          "building": {
      |            "number": "house number",
      |            "postcode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "zeroEmissionsCarAllowance": 3000.20
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 2000.10,
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse(
    """
      |{
      |    "ukOtherPropertyAnnualAllowances": {
      |      "annualInvestmentAllowance": 2000.50,
      |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
      |      "businessPremisesRenovationAllowance": 2000.70,
      |      "otherCapitalAllowance": 2000.80,
      |      "costOfReplacingDomesticGoods": 2000.90,
      |      "electricChargePointAllowance": 3000.10,
      |      "structuredBuildingAllowance": [
      |        {
      |          "amount": 3000.30,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.40
      |          },
      |          "building": {
      |            "name": "house name",
      |            "postCode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "enhancedStructuredBuildingAllowance": [
      |        {
      |          "amount": 3000.50,
      |          "firstYear": {
      |            "qualifyingDate": "2020-01-01",
      |            "qualifyingAmountExpenditure": 3000.60
      |          },
      |          "building": {
      |            "number": "house number",
      |            "postCode": "GF49JH"
      |          }
      |        }
      |      ],
      |      "zeroEmissionsCarAllowance": 3000.20
      |    },
      |    "ukOtherPropertyAnnualAdjustments": {
      |      "lossBroughtForward": 2000.10,
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "ukOtherRentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[UkNonFhlProperty] shouldBe requestBody
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
