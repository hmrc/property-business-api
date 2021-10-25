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

package v2.models.audit

import play.api.libs.json.Json
import support.UnitSpec

class CreateAndAmendForeignPropertyAnnualAuditDetailSpec extends UnitSpec {

  val validJson = Json.parse(
    """
      |{
      |"userType":"Agent",
      |"agentReferenceNumber":"012345678",
      |"nino":"JF902433C",
      |"businessId": "XAIS12345678910",
      |"taxYear":"2019-20",
      |"request": {
      |  "foreignFhlEea": {
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25,
      |        "periodOfGraceAdjustment":true
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    },
      |  "foreignProperty": [
      |    {
      |      "countryCode":"GER",
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "costOfReplacingDomesticItems":100.25,
      |        "zeroEmissionsGoodsVehicleAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "structureAndBuildingAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    }
      |  ]
      |},
      |"X-CorrelationId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
      |"response":{
      |        "httpStatus": 200,
      |         "body":{
      |            "links":[
      |            {
      |              "href": "/Individuals/business/property/AA123456A/$businessId/annual/2019-20",
      |              "method": "GET",
      |              "rel": "self"
      |            }
      |         ]
      |      }
      |   }
      |}
      |""".stripMargin
  )

  val validBody = CreateAndAmendForeignPropertyAnnualAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "JF902433C",
    businessId = "XAIS12345678910",
    taxYear = "2019-20",
    request = Json.parse(
      """
        |{
        |  "foreignFhlEea": {
        |      "adjustments": {
        |        "privateUseAdjustment":100.25,
        |        "balancingCharge":100.25,
        |        "periodOfGraceAdjustment":true
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance":100.25,
        |        "otherCapitalAllowance":100.25,
        |        "propertyAllowance":100.25,
        |        "electricChargePointAllowance":100.25
        |      }
        |    },
        |  "foreignProperty": [
        |    {
        |      "countryCode":"GER",
        |      "adjustments": {
        |        "privateUseAdjustment":100.25,
        |        "balancingCharge":100.25
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance":100.25,
        |        "costOfReplacingDomesticItems":100.25,
        |        "zeroEmissionsGoodsVehicleAllowance":100.25,
        |        "propertyAllowance":100.25,
        |        "otherCapitalAllowance":100.25,
        |        "structureAndBuildingAllowance":100.25,
        |        "electricChargePointAllowance":100.25
        |      }
        |    }
        |  ]
        |}
        |""".stripMargin
    ),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
    response = AuditResponse(
      200,
      Right(Some(Json.parse(
        """{
          |"links": [
          | {
          |  "href": "/Individuals/business/property/AA123456A/$businessId/annual/2019-20",
          |  "method": "GET",
          |  "rel": "self"
          | }
          |]
          |}""".stripMargin)))
    )
  )

  "writes" must {
    "work" when {
      "success response" in {
        Json.toJson(validBody) shouldBe validJson
      }
    }
  }
}