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

package v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def1_Retrieve_ForeignPropertyAllowancesSpec extends UnitSpec {

  private val foreignPropertyAllowances = Def1_Retrieve_ForeignPropertyAllowances(
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(
      List(
        Def1_Retrieve_StructuredBuildingAllowance(
          100.25,
          Some(
            Def1_Retrieve_FirstYear(
              "2020-03-29",
              100.25
            )),
          Def1_Retrieve_Building(
            Some("Building Name"),
            Some("12"),
            "TF3 4GH"
          )
        )))
  )

  private val jsonBody = Json.parse(
    """
      |{
      |          "annualInvestmentAllowance": 100.25,
      |          "costOfReplacingDomesticItems": 100.25,
      |          "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |          "otherCapitalAllowance": 100.25,
      |          "electricChargePointAllowance": 100.25,
      |          "zeroEmissionsCarAllowance": 100.25,
      |          "propertyIncomeAllowance": 100.25,
      |          "structuredBuildingAllowance": [
      |            {
      |              "amount": 100.25,
      |              "firstYear": {
      |                "qualifyingDate": "2020-03-29",
      |                "qualifyingAmountExpenditure": 100.25
      |              },
      |              "building": {
      |                "name": "Building Name",
      |                "number": "12",
      |                "postcode": "TF3 4GH"
      |              }
      |            }
      |          ]
      |        }
    """.stripMargin
  )

  private val ifsJsonBody = Json.parse(
    """
      |{
      |          "annualInvestmentAllowance": 100.25,
      |          "costOfReplacingDomesticItems": 100.25,
      |          "zeroEmissionsGoodsVehicleAllowance": 100.25,
      |          "otherCapitalAllowance": 100.25,
      |          "electricChargePointAllowance": 100.25,
      |          "zeroEmissionsCarAllowance": 100.25,
      |          "propertyAllowance": 100.25,
      |          "structuredBuildingAllowance": [
      |            {
      |              "amount": 100.25,
      |              "firstYear": {
      |                "qualifyingDate": "2020-03-29",
      |                "qualifyingAmountExpenditure": 100.25
      |              },
      |              "building": {
      |                "name": "Building Name",
      |                "number": "12",
      |                "postCode": "TF3 4GH"
      |              }
      |            }
      |          ]
      |        }
    """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        ifsJsonBody.as[Def1_Retrieve_ForeignPropertyAllowances] shouldBe foreignPropertyAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignPropertyAllowances) shouldBe jsonBody
      }
    }
  }

}
