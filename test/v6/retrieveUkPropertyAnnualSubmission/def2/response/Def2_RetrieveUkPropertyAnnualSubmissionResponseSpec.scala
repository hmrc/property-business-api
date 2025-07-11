/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveUkPropertyAnnualSubmission.def2.response

import play.api.libs.json.Json
import shared.models.domain.Timestamp
import shared.utils.UnitSpec
import v6.retrieveUkPropertyAnnualSubmission.def2.model.response._

class Def2_RetrieveUkPropertyAnnualSubmissionResponseSpec extends UnitSpec {

  private val tysDownstreamJson = Json.parse("""
      |{
      |   "submittedOn":"2020-06-17T10:53:38.000Z",
      |   "ukOtherProperty":{
      |      "ukOtherPropertyAnnualAllowances":{
      |         "annualInvestmentAllowance":678.45,
      |         "zeroEmissionGoodsVehicleAllowance":456.34,
      |         "businessPremisesRenovationAllowance":573.45,
      |         "otherCapitalAllowance":452.34,
      |         "costOfReplacingDomesticItems":567.34,
      |         "propertyIncomeAllowance":342.34,
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
      |         "rentARoom":{
      |            "jointlyLet":true
      |         }
      |      }
      |   }
      |}
      |""".stripMargin)

  private val parsed = Def2_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn = Timestamp("2020-06-17T10:53:38.000Z"),
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
            businessPremisesRenovationAllowance = Some(573.45),
            otherCapitalAllowance = Some(452.34),
            costOfReplacingDomesticItems = Some(567.34),
            propertyIncomeAllowance = Some(342.34),
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
      |   "ukProperty":{
      |      "allowances":{
      |         "annualInvestmentAllowance":678.45,
      |         "businessPremisesRenovationAllowance":573.45,
      |         "otherCapitalAllowance":452.34,
      |         "costOfReplacingDomesticItems":567.34,
      |         "propertyIncomeAllowance":342.34,
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
    "read tys JSON into a model" in {
      val result = tysDownstreamJson.as[Def2_RetrieveUkPropertyAnnualSubmissionResponse]
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
