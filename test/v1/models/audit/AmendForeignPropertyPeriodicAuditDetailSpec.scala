/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec

class AmendForeignPropertyPeriodicAuditDetailSpec extends UnitSpec {

  val validJson = Json.parse(
    """
      |{
      |      "userType":"Agent",
      |      "agentReferenceNumber":"012345678",
      |      "nino":"JF902433C",
      |      "businessId":"XAIS12345678910",
      |      "submissionId":"4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |      "request":{
      |         "foreignFhlEea": {
      |      "income": {
      |         "rentAmount": 567.83
      |      },
      |    "expenditure": {
      |      "premisesRunningCosts": 4567.98,
      |      "repairsAndMaintenance": 98765.67,
      |      "financialCosts": 4566.95,
      |      "professionalFees": 23.65,
      |      "costsOfServices": 4567.77,
      |      "travelCosts": 456.77,
      |      "other": 567.67,
      |      "consolidatedExpenses": 456.98
      |    }
      |  },
      |  "foreignProperty": [{
      |      "countryCode": "zzz",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 34456.30
      |        },
      |        "foreignTaxCreditRelief": true,
      |        "premiumOfLeaseGrant": 2543.43,
      |        "otherPropertyIncome": 54325.30,
      |        "foreignTaxTakenOff": 6543.01,
      |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5635.43,
      |        "repairsAndMaintenance": 3456.65,
      |        "financialCosts": 34532.21,
      |        "professionalFees": 32465.32,
      |        "costsOfServices": 2567.21,
      |        "travelCosts": 2345.76,
      |        "residentialFinancialCost": 21235.22,
      |        "broughtFwdResidentialFinancialCost": 12556.00,
      |        "other": 2425.11,
      |        "consolidatedExpenses": 352.66
      |      }
      |    }
      |  ]
      |},
      |"X-CorrelationId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
      |"response": {
      |"httpStatus": 200,
      |"body": {
      | "links":[
      |               {
      |                  "href":"/individuals/business/property/{nino}/{businessId}/period/{submissionId}",
      |                  "rel":"retrieve-property-period-summary",
      |                  "method":"GET"
      |               },
      |               {
      |                  "href":"/individuals/business/property/{nino}/{businessId}/period/{submissionId}",
      |                  "rel":"self",
      |                  "method":"PUT"
      |               },
      |               {
      |                  "href":"/individuals/business/property/{nino}/{businessId}/period",
      |                  "rel":"list-property-period-summaries",
      |                  "method":"GET"
      |               }
      |            ]
      |      }
      |   }
      |}
      |""".stripMargin
  )

  val validBody = AmendForeignPropertyPeriodicAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "JF902433C",
    businessId = "XAIS12345678910",
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    request = Json.parse(
      """
        |{
        |    "foreignFhlEea": {
        |      "income": {
        |         "rentAmount": 567.83
        |      },
        |    "expenditure": {
        |      "premisesRunningCosts": 4567.98,
        |      "repairsAndMaintenance": 98765.67,
        |      "financialCosts": 4566.95,
        |      "professionalFees": 23.65,
        |      "costsOfServices": 4567.77,
        |      "travelCosts": 456.77,
        |      "other": 567.67,
        |      "consolidatedExpenses": 456.98
        |    }
        |  },
        |  "foreignProperty": [{
        |      "countryCode": "zzz",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 34456.30
        |        },
        |        "foreignTaxCreditRelief": true,
        |        "premiumOfLeaseGrant": 2543.43,
        |        "otherPropertyIncome": 54325.30,
        |        "foreignTaxTakenOff": 6543.01,
        |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
        |      },
        |      "expenditure": {
        |        "premisesRunningCosts": 5635.43,
        |        "repairsAndMaintenance": 3456.65,
        |        "financialCosts": 34532.21,
        |        "professionalFees": 32465.32,
        |        "costsOfServices": 2567.21,
        |        "travelCosts": 2345.76,
        |        "residentialFinancialCost": 21235.22,
        |        "broughtFwdResidentialFinancialCost": 12556.00,
        |        "other": 2425.11,
        |        "consolidatedExpenses": 352.66
        |      }
        |    }
        |  ]
        |}
        |""".stripMargin
    ),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
    response = AuditResponse(
      200,
      Right(Some(Json.parse("""{
          |"links": [
          |  {
          |    "href":"/individuals/business/property/{nino}/{businessId}/period/{submissionId}",
          |    "rel":"retrieve-property-period-summary",
          |    "method":"GET"
          |  },
          |  {
          |    "href":"/individuals/business/property/{nino}/{businessId}/period/{submissionId}",
          |    "rel":"self",
          |    "method":"PUT"
          |  },
          |  {
          |    "href":"/individuals/business/property/{nino}/{businessId}/period",
          |    "rel":"list-property-period-summaries",
          |    "method":"GET"
          |  }
          |  ]
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
