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

package v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class UkNonFhlPropertySpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "ukOtherPropertyAnnualAllowances":{
      |      "annualInvestmentAllowance":678.45,
      |      "zeroEmissionGoodsVehicleAllowance":456.34,
      |      "businessPremisesRenovationAllowance":573.45,
      |      "otherCapitalAllowance":452.34,
      |      "costOfReplacingDomesticGoods":567.34,
      |      "propertyIncomeAllowance":342.34,
      |      "electricChargePointAllowance":454.34,
      |      "structuredBuildingAllowance":[
      |         {
      |            "amount":234.34,
      |            "firstYear":{
      |               "qualifyingDate":"2020-03-29",
      |               "qualifyingAmountExpenditure":3434.45
      |            },
      |            "building":{
      |               "name":"Plaza",
      |               "number":"1",
      |               "postCode":"TF3 4EH"
      |            }
      |         }
      |      ],
      |      "enhancedStructuredBuildingAllowance":[
      |         {
      |            "amount":234.45,
      |            "firstYear":{
      |               "qualifyingDate":"2020-05-29",
      |               "qualifyingAmountExpenditure":453.34
      |            },
      |            "building":{
      |               "name":"Plaza 2",
      |               "number":"2",
      |               "postCode":"TF3 4ER"
      |            }
      |         }
      |      ],
      |      "zeroEmissionsCarAllowance":454.34
      |   },
      |   "ukOtherPropertyAnnualAdjustments":{
      |      "balancingCharge":565.34,
      |      "privateUseAdjustment":533.54,
      |      "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |      "nonResidentLandlord":true,
      |      "ukOtherRentARoom":{
      |         "jointlyLet":true
      |      }
      |   }
      |}
      |""".stripMargin)

  val model: UkNonFhlProperty = UkNonFhlProperty(
    adjustments = Some(
      UkNonFhlPropertyAdjustments(
        balancingCharge = Some(565.34),
        privateUseAdjustment = Some(533.54),
        businessPremisesRenovationAllowanceBalancingCharges = Some(563.34),
        nonResidentLandlord = true,
        rentARoom = Some(
          UkNonFhlPropertyRentARoom(
            jointlyLet = true
          ))
      )
    ),
    allowances = Some(
      UkNonFhlPropertyAllowances(
        annualInvestmentAllowance = Some(678.45),
        zeroEmissionsGoodsVehicleAllowance = Some(456.34),
        businessPremisesRenovationAllowance = Some(573.45),
        otherCapitalAllowance = Some(452.34),
        costOfReplacingDomesticGoods = Some(567.34),
        propertyIncomeAllowance = Some(342.34),
        electricChargePointAllowance = Some(454.34),
        structuredBuildingAllowance = Some(
          Seq(
            UkNonFhlPropertyStructuredBuildingAllowance(
              amount = 234.34,
              firstYear = Some(
                UkNonFhlPropertyFirstYear(
                  qualifyingDate = "2020-03-29",
                  qualifyingAmountExpenditure = 3434.45
                )
              ),
              building = UkNonFhlPropertyBuilding(
                name = Some("Plaza"),
                number = Some("1"),
                postcode = "TF3 4EH"
              )
            )
          )),
        enhancedStructuredBuildingAllowance = Some(
          Seq(
            UkNonFhlPropertyStructuredBuildingAllowance(
              amount = 234.45,
              firstYear = Some(
                UkNonFhlPropertyFirstYear(
                  qualifyingDate = "2020-05-29",
                  qualifyingAmountExpenditure = 453.34
                )
              ),
              building = UkNonFhlPropertyBuilding(
                name = Some("Plaza 2"),
                number = Some("2"),
                postcode = "TF3 4ER"
              )
            )
          )),
        zeroEmissionsCarAllowance = Some(454.34)
      )
    )
  )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "allowances":{
      |      "annualInvestmentAllowance":678.45,
      |      "zeroEmissionsGoodsVehicleAllowance":456.34,
      |      "businessPremisesRenovationAllowance":573.45,
      |      "otherCapitalAllowance":452.34,
      |      "costOfReplacingDomesticGoods":567.34,
      |      "propertyIncomeAllowance":342.34,
      |      "electricChargePointAllowance":454.34,
      |      "structuredBuildingAllowance":[
      |         {
      |            "amount":234.34,
      |            "firstYear":{
      |               "qualifyingDate":"2020-03-29",
      |               "qualifyingAmountExpenditure":3434.45
      |            },
      |            "building":{
      |               "name":"Plaza",
      |               "number":"1",
      |               "postcode":"TF3 4EH"
      |            }
      |         }
      |      ],
      |      "enhancedStructuredBuildingAllowance":[
      |         {
      |            "amount":234.45,
      |            "firstYear":{
      |               "qualifyingDate":"2020-05-29",
      |               "qualifyingAmountExpenditure":453.34
      |            },
      |            "building":{
      |               "name":"Plaza 2",
      |               "number":"2",
      |               "postcode":"TF3 4ER"
      |            }
      |         }
      |      ],
      |      "zeroEmissionsCarAllowance":454.34
      |   },
      |   "adjustments":{
      |      "balancingCharge":565.34,
      |      "privateUseAdjustment":533.54,
      |      "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |      "nonResidentLandlord":true,
      |      "rentARoom":{
      |         "jointlyLet":true
      |      }
      |   }
      |}
      |""".stripMargin)

  "reads" should {
    "read JSON into a model" in {
      downstreamJson.as[UkNonFhlProperty] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
