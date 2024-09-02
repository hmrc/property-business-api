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

package auth

import api.services.DownstreamStub
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class PropertyBusinessApiAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "5.0"

  val supportingAgentsNotAllowedEndpoint = "amend-uk-property-annual-submission"

  val businessId: String = "XAIS12345678910"
  val taxYear            = "2022-23"

  val mtdUrl = s"/uk/$nino/$businessId/annual/$taxYear"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.put(requestBodyJson))

  val downstreamUri = s"/income-tax/business/property/annual"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.PUT

  override val downstreamSuccessStatus: Int = NO_CONTENT

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      s"""
         |{
         |  "links":[
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/annual/$taxYear",
         |      "method":"PUT",
         |      "rel":"create-and-amend-uk-property-annual-submission"
         |    },
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/annual/$taxYear",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/$nino/$businessId/annual/$taxYear",
         |      "method":"DELETE",
         |      "rel":"delete-property-annual-submission"
         |    }
         |  ]
         |}
      """.stripMargin
    )
  )

  val requestBodyJson: JsValue = Json.parse(
    """
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
      """.stripMargin
  )

}
