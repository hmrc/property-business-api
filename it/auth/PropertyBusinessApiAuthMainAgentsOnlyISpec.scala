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

import shared.services.DownstreamStub
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class PropertyBusinessApiAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "5.0"

  val supportingAgentsNotAllowedEndpoint = "retrieve-foreign-property-annual-submission"

  val mtdUrl = s"/foreign/AA123456A/XAIS12345678910/annual/2022-23"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri = s"/income-tax/business/property/annual"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      s"""
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
       |        "zeroEmissionsGoodsVehicleAllowance": 100.25,
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
    ))

}
