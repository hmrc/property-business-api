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

package v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty._
import v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukNonFhlProperty._
import v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukPropertyRentARoom.Def1_Create_Amend_UkPropertyAdjustmentsRentARoom
import v4.controllers.createAmendUkPropertyAnnualSubmission.model.request.Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody

class Def1_CreateAmendUkPropertyAnnualSubmissionRequestBodySpec extends UnitSpec {

  val requestBody: Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody =
    Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(
      Some(
        Def1_Create_Amend_UkFhlProperty(
          Some(
            Def1_Create_Amend_UkFhlPropertyAdjustments(
              Some(1000.20),
              Some(1000.30),
              true,
              Some(1000.40),
              true,
              Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true))
            )),
          Some(
            Def1_Create_Amend_UkFhlPropertyAllowances(
              Some(1000.50),
              Some(1000.60),
              Some(1000.70),
              Some(1000.80),
              Some(1000.90),
              None
            ))
        )),
      Some(
        Def1_Create_Amend_UkNonFhlProperty(
          Some(
            Def1_Create_Amend_UkNonFhlPropertyAdjustments(
              Some(2000.20),
              Some(2000.30),
              Some(2000.40),
              true,
              Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true))
            )),
          Some(Def1_Create_Amend_UkNonFhlPropertyAllowances(
            Some(2000.50),
            Some(2000.60),
            Some(2000.70),
            Some(2000.80),
            Some(2000.90),
            Some(3000.10),
            Some(3000.20),
            None,
            Some(
              Seq(
                Def1_Create_Amend_StructuredBuildingAllowance(
                  3000.30,
                  Some(Def1_Create_Amend_FirstYear(
                    "2020-01-01",
                    3000.40
                  )),
                  Def1_Create_Amend_Building(
                    Some("house name"),
                    None,
                    "GF49JH"
                  )
                ))),
            Some(
              Seq(
                Def1_Create_Amend_StructuredBuildingAllowance(
                  3000.50,
                  Some(Def1_Create_Amend_FirstYear(
                    "2020-01-01",
                    3000.60
                  )),
                  Def1_Create_Amend_Building(
                    None,
                    Some("house number"),
                    "GF49JH"
                  )
                )))
          ))
        ))
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |  "ukFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 1000.50,
      |      "businessPremisesRenovationAllowance": 1000.60,
      |      "otherCapitalAllowance": 1000.70,
      |      "electricChargePointAllowance": 1000.80,
      |      "zeroEmissionsCarAllowance": 1000.90
      |    },
      |    "adjustments": {
      |      "privateUseAdjustment": 1000.20,
      |      "balancingCharge": 1000.30,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
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
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  }
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |  "ukFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 1000.50,
      |      "businessPremisesRenovationAllowance": 1000.60,
      |      "otherCapitalAllowance": 1000.70,
      |      "electricChargePointAllowance": 1000.80,
      |      "zeroEmissionsCarAllowance": 1000.90
      |    },
      |    "adjustments": {
      |      "privateUseAdjustment": 1000.20,
      |      "balancingCharge": 1000.30,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges":1000.40,
      |      "nonResidentLandlord": true,
      |      "ukFhlRentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  },
      |  "ukOtherProperty": {
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
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "ukOtherRentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody] shouldBe requestBody
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
