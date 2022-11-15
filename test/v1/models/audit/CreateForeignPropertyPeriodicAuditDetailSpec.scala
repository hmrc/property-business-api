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

class CreateForeignPropertyPeriodicAuditDetailSpec extends UnitSpec {

  val validJson = Json.parse(
    """
      |{
      |	"userType": "Agent",
      |	"agentReferenceNumber": "012345678",
      |	"nino": "JF902433C",
      |	"businessId": "XAIS12345678910",
      |	"request": {
      |		"fromDate": "2020-01-01",
      |		"toDate": "2020-01-31",
      |		"foreignFhlEea": {
      |			"income": {
      |				"rentAmount": 5000.99
      |			},
      |			"expenditure": {
      |				"premisesRunningCosts": 5000.99,
      |				"repairsAndMaintenance": 5000.99,
      |				"financialCosts": 5000.99,
      |				"professionalFees": 5000.99,
      |				"costsOfServices": 5000.99,
      |				"travelCosts": 5000.99,
      |				"other": 5000.99,
      |				"consolidatedExpenses": 5000.99
      |			}
      |		},
      |		"foreignProperty": [{
      |			"countryCode": "FRA",
      |			"income": {
      |				"rentIncome": {
      |					"rentAmount": 5000.99
      |				},
      |				"foreignTaxCreditRelief": false,
      |				"premiumOfLeaseGrant": 5000.99,
      |				"otherPropertyIncome": 5000.99,
      |				"foreignTaxTakenOff": 5000.99,
      |				"specialWithholdingTaxOrUKTaxPaid": 5000.99
      |			},
      |			"expenditure": {
      |				"premisesRunningCosts": 5000.99,
      |				"repairsAndMaintenance": 5000.99,
      |				"financialCosts": 5000.99,
      |				"professionalFees": 5000.99,
      |				"costsOfServices": 5000.99,
      |				"travelCosts": 5000.99,
      |				"residentialFinancialCost": 5000.99,
      |				"broughtFwdResidentialFinancialCost": 5000.99,
      |				"other": 5000.99,
      |				"consolidatedExpenses": 5000.99
      |			}
      |		}]
      |	},
      |"X-CorrelationId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
      |	"response": {
      |		"httpStatus": 200,
      |		"body": {
      |  		"submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |			"links": [{
      |				"href": "/individuals/business/property/TC663795B/XAIS12345678910/period",
      |				"method": "GET",
      |				"rel": "self"
      |			}]
      |		}
      |	}
      |}""".stripMargin
  )

  val validBody = CreateForeignPropertyPeriodicAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "JF902433C",
    businessId = "XAIS12345678910",
    request = Json.parse(
      """{
        |"fromDate": "2020-01-01",
        |  "toDate": "2020-01-31",
        |  "foreignFhlEea": {
        |    "income": {
        |      "rentAmount": 5000.99
        |    },
        |    "expenditure": {
        |      "premisesRunningCosts": 5000.99,
        |      "repairsAndMaintenance": 5000.99,
        |      "financialCosts": 5000.99,
        |      "professionalFees": 5000.99,
        |      "costsOfServices": 5000.99,
        |      "travelCosts": 5000.99,
        |      "other": 5000.99,
        |      "consolidatedExpenses": 5000.99
        |    }
        |  },
        |  "foreignProperty": [
        |    {
        |      "countryCode": "FRA",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 5000.99
        |        },
        |        "foreignTaxCreditRelief": false,
        |        "premiumOfLeaseGrant": 5000.99,
        |        "otherPropertyIncome": 5000.99,
        |        "foreignTaxTakenOff": 5000.99,
        |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
        |      },
        |      "expenditure": {
        |        "premisesRunningCosts": 5000.99,
        |        "repairsAndMaintenance": 5000.99,
        |        "financialCosts": 5000.99,
        |        "professionalFees": 5000.99,
        |        "costsOfServices": 5000.99,
        |        "travelCosts": 5000.99,
        |        "residentialFinancialCost": 5000.99,
        |        "broughtFwdResidentialFinancialCost": 5000.99,
        |        "other": 5000.99,
        |        "consolidatedExpenses": 5000.99
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
          |"submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |"links": [
          |{
          |  "href":"/individuals/business/property/TC663795B/XAIS12345678910/period",
          |  "method":"GET",
          |  "rel":"self"
          |}
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
