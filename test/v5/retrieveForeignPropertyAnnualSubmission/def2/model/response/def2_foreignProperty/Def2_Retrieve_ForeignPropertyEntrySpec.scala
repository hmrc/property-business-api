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

package v5.retrieveForeignPropertyAnnualSubmission.def2.model.response.def2_foreignProperty

import play.api.libs.json.Json
import support.UnitSpec

class Def2_Retrieve_ForeignPropertyEntrySpec extends UnitSpec {

  private val foreignProperty = Def2_Retrieve_ForeignPropertyEntry(
    "GER",
    Some(
      Def2_Retrieve_ForeignPropertyAdjustments(
        Some(100.25),
        Some(100.25)
      )),
    Some(
      Def2_Retrieve_ForeignPropertyAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(
          List(
            Def2_Retrieve_StructuredBuildingAllowance(
              100.25,
              Some(Def2_Retrieve_FirstYear(
                "2020-03-29",
                100.25
              )),
              Def2_Retrieve_Building(
                Some("Building Name"),
                Some("12"),
                "TF3 4GH"
              )
            )))
      ))
  )

  private val foreignPropertyNoAdjustments = Def2_Retrieve_ForeignPropertyEntry(
    "GER",
    None,
    Some(
      Def2_Retrieve_ForeignPropertyAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(
          List(
            Def2_Retrieve_StructuredBuildingAllowance(
              100.25,
              Some(Def2_Retrieve_FirstYear(
                "2020-03-29",
                100.25
              )),
              Def2_Retrieve_Building(
                Some("Building Name"),
                Some("12"),
                "TF3 4GH"
              )
            )))
      ))
  )

  private val foreignPropertyNoAllowances = Def2_Retrieve_ForeignPropertyEntry(
    "GER",
    Some(
      Def2_Retrieve_ForeignPropertyAdjustments(
        Some(100.25),
        Some(100.25)
      )),
    None
  )

  private val jsonBody = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25
      |   },
      |      "allowances": {
      |        "annualInvestmentAllowance": 100.25,
      |        "costOfReplacingDomesticItems": 100.25,
      |        "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |        "otherCapitalAllowance": 100.25,
      |        "zeroEmissionsCarAllowance": 100.25,
      |        "propertyIncomeAllowance": 100.25,
      |        "structuredBuildingAllowance": [
      |          {
      |            "amount": 100.25,
      |            "firstYear": {
      |              "qualifyingDate": "2020-03-29",
      |              "qualifyingAmountExpenditure": 100.25
      |            },
      |            "building": {
      |              "name": "Building Name",
      |              "number": "12",
      |              "postcode": "TF3 4GH"
      |            }
      |          }
      |        ]
      |      }
      |}
    """.stripMargin
  )

  private val ifsJsonBody = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25
      |   },
      |      "allowances": {
      |        "annualInvestmentAllowance": 100.25,
      |        "costOfReplacingDomesticItems": 100.25,
      |        "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |        "otherCapitalAllowance": 100.25,
      |        "zeroEmissionsCarAllowance": 100.25,
      |        "propertyAllowance": 100.25,
      |        "structuredBuildingAllowance": [
      |          {
      |            "amount": 100.25,
      |            "firstYear": {
      |              "qualifyingDate": "2020-03-29",
      |              "qualifyingAmountExpenditure": 100.25
      |            },
      |            "building": {
      |              "name": "Building Name",
      |              "number": "12",
      |              "postCode": "TF3 4GH"
      |            }
      |          }
      |        ]
      |      }
      |}
    """.stripMargin
  )

  private val jsonBodyNoAdjustments = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "allowances": {
      |     "annualInvestmentAllowance": 100.25,
      |     "costOfReplacingDomesticItems": 100.25,
      |     "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |     "otherCapitalAllowance": 100.25,
      |     "zeroEmissionsCarAllowance": 100.25,
      |     "propertyIncomeAllowance": 100.25,
      |     "structuredBuildingAllowance": [
      |          {
      |            "amount": 100.25,
      |            "firstYear": {
      |              "qualifyingDate": "2020-03-29",
      |              "qualifyingAmountExpenditure": 100.25
      |            },
      |            "building": {
      |              "name": "Building Name",
      |              "number": "12",
      |              "postcode": "TF3 4GH"
      |            }
      |          }
      |        ]
      |      }
      |}
    """.stripMargin
  )

  private val ifsJsonBodyNoAdjustments = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "allowances": {
      |     "annualInvestmentAllowance": 100.25,
      |     "costOfReplacingDomesticItems": 100.25,
      |     "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |     "otherCapitalAllowance": 100.25,
      |     "zeroEmissionsCarAllowance": 100.25,
      |     "propertyAllowance": 100.25,
      |     "structuredBuildingAllowance": [
      |          {
      |            "amount": 100.25,
      |            "firstYear": {
      |              "qualifyingDate": "2020-03-29",
      |              "qualifyingAmountExpenditure": 100.25
      |            },
      |            "building": {
      |              "name": "Building Name",
      |              "number": "12",
      |              "postCode": "TF3 4GH"
      |            }
      |          }
      |        ]
      |      }
      |}
    """.stripMargin
  )

  private val jsonBodyNoAllowances = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25
      |   }
      |}
     """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        ifsJsonBody.as[Def2_Retrieve_ForeignPropertyEntry] shouldBe foreignProperty
      }
      "return a valid model with no adjustments object" in {
        ifsJsonBodyNoAdjustments.as[Def2_Retrieve_ForeignPropertyEntry] shouldBe foreignPropertyNoAdjustments
      }
      "return a valid model with no allowances object" in {
        jsonBodyNoAllowances.as[Def2_Retrieve_ForeignPropertyEntry] shouldBe foreignPropertyNoAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignProperty) shouldBe jsonBody
      }
      "return a valid JSON with no adjustments" in {
        Json.toJson(foreignPropertyNoAdjustments) shouldBe jsonBodyNoAdjustments
      }
      "return a valid JSON with no allowances" in {
        Json.toJson(foreignPropertyNoAllowances) shouldBe jsonBodyNoAllowances
      }
    }
  }

}
