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

package v5.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class CreateAmendUkPropertyAllowancesSpec extends UnitSpec {

  val requestBody: CreateAmendUkPropertyAllowances =
    CreateAmendUkPropertyAllowances(
      Some(2000.50),
      Some(2000.60),
      Some(2000.70),
      Some(2000.80),
      Some(2000.90),
      Some(3000.10),
      Some(3000.20),
      None,
      Some(
        List(
          CreateAmendStructuredBuildingAllowance(
            3000.30,
            Some(
              CreateAmendFirstYear(
                "2020-01-01",
                3000.40
              )),
            CreateAmendBuilding(
              Some("house name"),
              None,
              "GF49JH"
            )
          ))),
      Some(
        List(
          CreateAmendStructuredBuildingAllowance(
            3000.50,
            Some(
              CreateAmendFirstYear(
                "2020-01-01",
                3000.60
              )),
            CreateAmendBuilding(
              None,
              Some("house number"),
              "GF49JH"
            )
          )))
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |      "annualInvestmentAllowance": 2000.50,
      |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |      "businessPremisesRenovationAllowance": 2000.70,
      |      "otherCapitalAllowance": 2000.80,
      |      "costOfReplacingDomesticItems": 2000.90,
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
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
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
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[CreateAmendUkPropertyAllowances] shouldBe requestBody
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
