/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.response.retrieveForeignPropertyAnnualSubmission

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v2.models.hateoas.{Link, Method}
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances, ForeignFhlEeaEntry}
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty.{Building, FirstYear, ForeignPropertyAdjustments, ForeignPropertyAllowances, ForeignPropertyEntry, StructuredBuildingAllowance}

class RetrieveForeignPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  private val retrieveForeignPropertyAnnualSubmissionResponseBody = RetrieveForeignPropertyAnnualSubmissionResponse(
    "2020-07-07T10:59:47.544Z",
    Some(ForeignFhlEeaEntry(
      Some(ForeignFhlEeaAdjustments(
        Some(100.25),
        Some(100.25),
        Some(true))),
      Some(ForeignFhlEeaAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25)))
    )),
    Some(Seq(ForeignPropertyEntry(
      "GER",
      Some(ForeignPropertyAdjustments(
        Some(100.25),
        Some(100.25))),
      Some(ForeignPropertyAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(Seq(StructuredBuildingAllowance(
          100.25,
          Some(FirstYear(
            "2020-03-29",
            100.25
          )),
          Building(
            Some("Building Name"),
            Some("12"),
            "TF3 4GH"
          )
        )))))
    )))
  )

  private val retrieveForeignPropertyAnnualSubmissionResponseBodyMinimum = RetrieveForeignPropertyAnnualSubmissionResponse(
    "2020-07-07T10:59:47.544Z",
    None,
    Some(Seq(ForeignPropertyEntry(
      "GER",
      None,
      None
    )))
  )

  private val jsonBody = Json.parse(
    """
      |{
      |  "submittedOn": "2020-07-07T10:59:47.544Z",
      |  "foreignFhlEea": {
      |    "adjustments": {
      |      "privateUseAdjustment": 100.25,
      |      "balancingCharge": 100.25,
      |      "periodOfGraceAdjustment": true
      |    },
      |    "allowances": {
      |      "annualInvestmentAllowance": 100.25,
      |      "otherCapitalAllowance": 100.25,
      |      "electricChargePointAllowance": 100.25,
      |      "zeroEmissionsCarAllowance": 100.25,
      |      "propertyIncomeAllowance": 100.25
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "GER",
      |      "adjustments": {
      |        "privateUseAdjustment": 100.25,
      |        "balancingCharge": 100.25
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance": 100.25,
      |        "costOfReplacingDomesticItems": 100.25,
      |        "zeroEmissionGoodsVehicleAllowance": 100.25,
      |        "otherCapitalAllowance": 100.25,
      |        "electricChargePointAllowance": 100.25,
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
      |    }
      |  ]
      |}
     """.stripMargin
  )

  private val ifsJson = Json.parse(
    """
      |{
      |    "submittedOn": "2020-07-07T10:59:47.544Z",
      |    "deletedOn": "2021-11-04T08:23:42Z",
      |    "foreignFhlEea": {
      |      "adjustments": {
      |        "privateUseAdjustment": 100.25,
      |        "balancingCharge": 100.25,
      |        "periodOfGraceAdjustment": true
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance": 100.25,
      |        "otherCapitalAllowance": 100.25,
      |        "electricChargePointAllowance": 100.25,
      |        "zeroEmissionsCarAllowance": 100.25,
      |        "propertyAllowance": 100.25
      |      }
      |    },
      |    "foreignProperty": [
      |      {
      |        "countryCode": "GER",
      |        "adjustments": {
      |          "privateUseAdjustment": 100.25,
      |          "balancingCharge": 100.25
      |        },
      |        "allowances": {
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
      |      }
      |    ]
      |  }
      |""".stripMargin)

  private val ifsJsonBodyMinimum = Json.parse(
    """
      |{
      |  "submittedOn": "2020-07-07T10:59:47.544Z",
      |  "foreignProperty": [
      |    {
      |      "countryCode": "GER"
      |    }
      |  ]
      |}
     """.stripMargin
  )

  private val jsonBodyMinimum = Json.parse(
    """
      |{
      |  "submittedOn": "2020-07-07T10:59:47.544Z",
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "GER"
      |    }
      |  ]
      |}
     """.stripMargin
  )

  "reads" should {
    "read JSON into a model" in {
      ifsJson.as[RetrieveForeignPropertyAnnualSubmissionResponse] shouldBe retrieveForeignPropertyAnnualSubmissionResponseBody
    }
    "read the minimum JSON into a model" in {
      ifsJsonBodyMinimum.as[RetrieveForeignPropertyAnnualSubmissionResponse] shouldBe retrieveForeignPropertyAnnualSubmissionResponseBodyMinimum
    }

  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(retrieveForeignPropertyAnnualSubmissionResponseBody) shouldBe jsonBody
    }
    "write a minimum model to a JSON" in {
      Json.toJson(retrieveForeignPropertyAnnualSubmissionResponseBodyMinimum) shouldBe jsonBodyMinimum
    }
  }

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: RetrieveForeignPropertyAnnualSubmissionHateoasData =
          RetrieveForeignPropertyAnnualSubmissionHateoasData("myNino", "myBusinessId", "mySubmissionId")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveForeignPropertyAnnualSubmissionResponse.RetrieveAnnualSubmissionLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(href = s"/my/context/foreign/${data.nino}/${data.businessId}/annual/${data.taxYear}", method = Method.PUT, rel = "create-and-amend-foreign-property-annual-submission"),
          Link(href = s"/my/context/foreign/${data.nino}/${data.businessId}/annual/${data.taxYear}", method = Method.GET, rel = "self"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/annual/${data.taxYear}", method = Method.DELETE, rel = "delete-property-annual-submission")
        )
      }
    }
  }
}