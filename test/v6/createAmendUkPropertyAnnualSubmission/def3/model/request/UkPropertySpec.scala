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

package v6.createAmendUkPropertyAnnualSubmission.def3.model.request

import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json}

class UkPropertySpec extends UnitSpec {

  val requestBody: UkProperty =
    UkProperty(
      Some(
        Adjustments(
          Some(2000.20),
          Some(2000.30),
          Some(2000.40),
          true,
          Some(RentARoom(true))
        )),
      Some(
        Allowances(
          Some(2000.50),
          Some(2000.60),
          Some(2000.70),
          Some(2000.80),
          Some(2000.90),
          Some(3000.80),
          Some(3000.90),
          None,
          Some(
            List(
              StructuredBuildingAllowance(
                3000.30,
                Some(FirstYear(
                  "2020-01-01",
                  3000.40
                )),
                Building(
                  Some("house name"),
                  None,
                  "GF4 9JH"
                )
              ))),
          Some(
            List(
              StructuredBuildingAllowance(
                3000.50,
                Some(FirstYear(
                  "2020-01-01",
                  3000.60
                )),
                Building(
                  None,
                  Some("house number"),
                  "GF4 9JH"
                )
              )))
        ))
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |  "allowances": {
      |    "annualInvestmentAllowance": 2000.50,
      |    "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |    "businessPremisesRenovationAllowance": 2000.70,
      |    "otherCapitalAllowance": 2000.80,
      |    "costOfReplacingDomesticItems": 2000.90,
      |    "zeroEmissionsCarAllowance": 3000.80,
      |    "firstYearAllowanceOnPlantAndMachinery": 3000.90,
      |    "structuredBuildingAllowance": [
      |      {
      |        "amount": 3000.30,
      |        "firstYear": {
      |          "qualifyingDate": "2020-01-01",
      |          "qualifyingAmountExpenditure": 3000.40
      |        },
      |        "building": {
      |          "name": "house name",
      |          "postcode": "GF4 9JH"
      |        }
      |      }
      |    ],
      |    "enhancedStructuredBuildingAllowance": [
      |      {
      |        "amount": 3000.50,
      |        "firstYear": {
      |          "qualifyingDate": "2020-01-01",
      |          "qualifyingAmountExpenditure": 3000.60
      |        },
      |        "building": {
      |          "number": "house number",
      |          "postcode": "GF4 9JH"
      |        }
      |      }
      |    ]
      |  },
      |  "adjustments": {
      |    "balancingCharge": 2000.20,
      |    "privateUseAdjustment": 2000.30,
      |    "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |    "nonResidentLandlord": true,
      |    "rentARoom": {
      |      "jointlyLet": true
      |    }
      |  }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |  "ukOtherPropertyAnnualAdjustments": {
      |    "balancingCharge": 2000.20,
      |    "privateUseAdjustment": 2000.30,
      |    "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |    "nonResidentLandlord": true,
      |    "ukOtherRentARoom": {
      |      "jointlyLet": true
      |    }
      |  },
      |  "ukOtherPropertyAnnualAllowances": {
      |    "annualInvestmentAllowance": 2000.50,
      |    "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |    "businessPremisesRenovationAllowance": 2000.70,
      |    "otherCapitalAllowance": 2000.80,
      |    "costOfReplacingDomesticGoods": 2000.90,
      |    "zeroEmissionsCarAllowance": 3000.80,
      |    "firstYearAllowanceOnPlantAndMachinery": 3000.90,
      |    "structuredBuildingAllowance": [
      |      {
      |        "amount": 3000.30,
      |        "firstYear": {
      |          "qualifyingDate": "2020-01-01",
      |          "qualifyingAmountExpenditure": 3000.40
      |        },
      |        "building": {
      |          "name": "house name",
      |          "postCode": "GF4 9JH"
      |        }
      |      }
      |    ],
      |    "enhancedStructuredBuildingAllowance": [
      |      {
      |        "amount": 3000.50,
      |        "firstYear": {
      |          "qualifyingDate": "2020-01-01",
      |          "qualifyingAmountExpenditure": 3000.60
      |        },
      |        "building": {
      |          "number": "house number",
      |          "postCode": "GF4 9JH"
      |        }
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[UkProperty] shouldBe requestBody
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
