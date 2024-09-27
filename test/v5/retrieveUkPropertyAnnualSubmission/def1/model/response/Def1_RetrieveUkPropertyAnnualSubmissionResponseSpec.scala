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

package v5.retrieveUkPropertyAnnualSubmission.def1.model.response

import api.models.domain.Timestamp
import config.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty._
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty._
import v5.retrieveUkPropertyAnnualSubmission.model.response._

class Def1_RetrieveUkPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  private val preTysDownstreamJson = Json.parse("""
      |{
      |   "submittedOn":"2020-06-17T10:53:38.000Z",
      |   "ukFhlProperty":{
      |      "allowances":{
      |         "annualInvestmentAllowance":123.45,
      |         "businessPremisesRenovationAllowance":345.56,
      |         "otherCapitalAllowance":345.34,
      |         "propertyIncomeAllowance":453.45,
      |         "electricChargePointAllowance":453.34,
      |         "zeroEmissionsCarAllowance":123.12
      |      },
      |      "adjustments":{
      |         "privateUseAdjustment":454.45,
      |         "balancingCharge":231.45,
      |         "periodOfGraceAdjustment":true,
      |         "businessPremisesRenovationAllowanceBalancingCharges":567.67,
      |         "nonResidentLandlord":true,
      |         "ukFhlRentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   },
      |   "ukOtherProperty":{
      |      "ukOtherPropertyAnnualAllowances":{
      |         "annualInvestmentAllowance":678.45,
      |         "zeroEmissionGoodsVehicleAllowance":456.34,
      |         "businessPremisesRenovationAllowance":573.45,
      |         "otherCapitalAllowance":452.34,
      |         "costOfReplacingDomesticGoods":567.34,
      |         "propertyIncomeAllowance":342.34,
      |         "electricChargePointAllowance":454.34,
      |         "structuredBuildingAllowance":[
      |            {
      |               "amount":234.34,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-03-29",
      |                  "qualifyingAmountExpenditure":3434.45
      |               },
      |               "building":{
      |                  "name":"Plaza",
      |                  "number":"1",
      |                  "postCode":"TF3 4EH"
      |               }
      |            }
      |         ],
      |         "enhancedStructuredBuildingAllowance":[
      |            {
      |               "amount":234.45,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-05-29",
      |                  "qualifyingAmountExpenditure":453.34
      |               },
      |               "building":{
      |                  "name":"Plaza 2",
      |                  "number":"2",
      |                  "postCode":"TF3 4ER"
      |               }
      |            }
      |         ],
      |         "zeroEmissionsCarAllowance":454.34
      |      },
      |      "ukOtherPropertyAnnualAdjustments":{
      |         "balancingCharge":565.34,
      |         "privateUseAdjustment":533.54,
      |         "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |         "nonResidentLandlord":true,
      |         "ukOtherRentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   }
      |}
      |""".stripMargin)

  private val tysDownstreamJson = Json.parse("""
      |{
      |   "submittedOn":"2020-06-17T10:53:38.000Z",
      |   "ukFhlProperty":{
      |      "allowances":{
      |         "annualInvestmentAllowance":123.45,
      |         "businessPremisesRenovationAllowance":345.56,
      |         "otherCapitalAllowance":345.34,
      |         "propertyIncomeAllowance":453.45,
      |         "electricChargePointAllowance":453.34,
      |         "zeroEmissionsCarAllowance":123.12
      |      },
      |      "adjustments":{
      |         "privateUseAdjustment":454.45,
      |         "balancingCharge":231.45,
      |         "periodOfGraceAdjustment":true,
      |         "businessPremisesRenovationAllowanceBalancingCharges":567.67,
      |         "nonResidentLandlord":true,
      |         "ukFhlRentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   },
      |   "ukOtherProperty":{
      |      "ukOtherPropertyAnnualAllowances":{
      |         "annualInvestmentAllowance":678.45,
      |         "zeroEmissionGoodsVehicleAllowance":456.34,
      |         "businessPremisesRenovationAllowance":573.45,
      |         "otherCapitalAllowance":452.34,
      |         "costOfReplacingDomesticItems":567.34,
      |         "propertyIncomeAllowance":342.34,
      |         "electricChargePointAllowance":454.34,
      |         "structuredBuildingAllowance":[
      |            {
      |               "amount":234.34,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-03-29",
      |                  "qualifyingAmountExpenditure":3434.45
      |               },
      |               "building":{
      |                  "name":"Plaza",
      |                  "number":"1",
      |                  "postCode":"TF3 4EH"
      |               }
      |            }
      |         ],
      |         "enhancedStructuredBuildingAllowance":[
      |            {
      |               "amount":234.45,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-05-29",
      |                  "qualifyingAmountExpenditure":453.34
      |               },
      |               "building":{
      |                  "name":"Plaza 2",
      |                  "number":"2",
      |                  "postCode":"TF3 4ER"
      |               }
      |            }
      |         ],
      |         "zeroEmissionsCarAllowance":454.34
      |      },
      |      "ukOtherPropertyAnnualAdjustments":{
      |         "balancingCharge":565.34,
      |         "privateUseAdjustment":533.54,
      |         "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |         "nonResidentLandlord":true,
      |         "ukOtherRentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   }
      |}
      |""".stripMargin)

  private val parsed = Def1_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn = Timestamp("2020-06-17T10:53:38.000Z"),
    ukFhlProperty = Some(
      RetrieveUkFhlProperty(
        adjustments = Some(
          RetrieveUkFhlPropertyAdjustments(
            privateUseAdjustment = Some(454.45),
            balancingCharge = Some(231.45),
            periodOfGraceAdjustment = true,
            businessPremisesRenovationAllowanceBalancingCharges = Some(567.67),
            nonResidentLandlord = true,
            rentARoom = Some(
              RetrieveUkFhlPropertyRentARoom(
                jointlyLet = true
              ))
          )
        ),
        allowances = Some(
          RetrieveUkFhlPropertyAllowances(
            Some(123.45),
            Some(345.56),
            Some(345.34),
            Some(453.45),
            Some(453.34),
            Some(123.12)
          )
        )
      )
    ),
    ukProperty = Some(
      RetrieveUkProperty(
        adjustments = Some(
          RetrieveUkPropertyAdjustments(
            balancingCharge = Some(565.34),
            privateUseAdjustment = Some(533.54),
            businessPremisesRenovationAllowanceBalancingCharges = Some(563.34),
            nonResidentLandlord = true,
            rentARoom = Some(
              RetrieveUkPropertyRentARoom(
                jointlyLet = true
              ))
          )
        ),
        allowances = Some(
          RetrieveUkPropertyAllowances(
            annualInvestmentAllowance = Some(678.45),
            zeroEmissionsGoodsVehicleAllowance = Some(456.34),
            businessPremisesRenovationAllowance = Some(573.45),
            otherCapitalAllowance = Some(452.34),
            costOfReplacingDomesticGoods = Some(567.34),
            propertyIncomeAllowance = Some(342.34),
            electricChargePointAllowance = Some(454.34),
            structuredBuildingAllowance = Some(
              List(
                RetrieveUkPropertyStructuredBuildingAllowance(
                  amount = 234.34,
                  firstYear = Some(
                    RetrieveUkPropertyFirstYear(
                      qualifyingDate = "2020-03-29",
                      qualifyingAmountExpenditure = 3434.45
                    )
                  ),
                  building = RetrieveUkPropertyBuilding(
                    name = Some("Plaza"),
                    number = Some("1"),
                    postcode = "TF3 4EH"
                  )
                )
              )),
            enhancedStructuredBuildingAllowance = Some(
              List(
                RetrieveUkPropertyStructuredBuildingAllowance(
                  amount = 234.45,
                  firstYear = Some(
                    RetrieveUkPropertyFirstYear(
                      qualifyingDate = "2020-05-29",
                      qualifyingAmountExpenditure = 453.34
                    )
                  ),
                  building = RetrieveUkPropertyBuilding(
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
    )
  )

  private val mtdJson = Json.parse("""
      |{
      |   "submittedOn":"2020-06-17T10:53:38.000Z",
      |   "ukFhlProperty":{
      |      "allowances":{
      |         "annualInvestmentAllowance":123.45,
      |         "businessPremisesRenovationAllowance":345.56,
      |         "otherCapitalAllowance":345.34,
      |         "propertyIncomeAllowance":453.45,
      |         "electricChargePointAllowance":453.34,
      |         "zeroEmissionsCarAllowance":123.12
      |      },
      |      "adjustments":{
      |         "privateUseAdjustment":454.45,
      |         "balancingCharge":231.45,
      |         "periodOfGraceAdjustment":true,
      |         "businessPremisesRenovationAllowanceBalancingCharges":567.67,
      |         "nonResidentLandlord":true,
      |         "rentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   },
      |   "ukProperty":{
      |      "allowances":{
      |         "annualInvestmentAllowance":678.45,
      |         "zeroEmissionsGoodsVehicleAllowance":456.34,
      |         "businessPremisesRenovationAllowance":573.45,
      |         "otherCapitalAllowance":452.34,
      |         "costOfReplacingDomesticGoods":567.34,
      |         "propertyIncomeAllowance":342.34,
      |         "electricChargePointAllowance":454.34,
      |         "structuredBuildingAllowance":[
      |            {
      |               "amount":234.34,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-03-29",
      |                  "qualifyingAmountExpenditure":3434.45
      |               },
      |               "building":{
      |                  "name":"Plaza",
      |                  "number":"1",
      |                  "postcode":"TF3 4EH"
      |               }
      |            }
      |         ],
      |         "enhancedStructuredBuildingAllowance":[
      |            {
      |               "amount":234.45,
      |               "firstYear":{
      |                  "qualifyingDate":"2020-05-29",
      |                  "qualifyingAmountExpenditure":453.34
      |               },
      |               "building":{
      |                  "name":"Plaza 2",
      |                  "number":"2",
      |                  "postcode":"TF3 4ER"
      |               }
      |            }
      |         ],
      |         "zeroEmissionsCarAllowance":454.34
      |      },
      |      "adjustments":{
      |         "balancingCharge":565.34,
      |         "privateUseAdjustment":533.54,
      |         "businessPremisesRenovationAllowanceBalancingCharges":563.34,
      |         "nonResidentLandlord":true,
      |         "rentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   }
      |}
      |""".stripMargin)

  "reads" should {
    "read pre tys JSON into a model" in {
      val result: RetrieveUkPropertyAnnualSubmissionResponse = preTysDownstreamJson.as[Def1_RetrieveUkPropertyAnnualSubmissionResponse]
      result shouldBe parsed
    }

    "read tys JSON into a model" in {
      val result = tysDownstreamJson.as[Def1_RetrieveUkPropertyAnnualSubmissionResponse]
      result shouldBe parsed
    }
  }

  "writes" should {
    "write a model to JSON" in {
      val result = Json.toJson(parsed)
      result shouldBe mtdJson
    }
  }

}
