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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import play.api.test.Helpers.AUTHORIZATION
import support.V1IntegrationBaseSpec
import v1.stubs.{ AuditStub, AuthStub, IfsStub, MtdIdLookupStub }

class AuthISpec extends V1IntegrationBaseSpec {

  private trait Test {
    val nino: String         = "AA123456A"
    val businessId: String   = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val ifsResponseBody: JsValue = Json.parse(
      """
        |{
        |  "fromDate": "2019-04-06",
        |  "toDate": "2019-07-06",
        |  "foreignFhlEea": {
        |        "income": {
        |          "rentAmount": 200.22,
        |          "taxDeducted": 22.22
        |        },
        |        "expenses": {
        |          "premisesRunningCostsAmount": 100.25,
        |          "repairsAndMaintenanceAmount": 100.25,
        |          "financialCostsAmount": 100.25,
        |          "professionalFeesAmount": 100.25,
        |          "costOfServicesAmount": 100.25,
        |          "travelCostsAmount": 100.25,
        |          "otherAmount": 100.25
        |        }
        |      },
        |  "foreignProperty": [
        |      {
        |        "countryCode": "FRA",
        |        "income": {
        |            "rentIncome": {
        |                "rentAmount": 200.22,
        |                "taxDeducted": 22.22
        |            },
        |          "foreignTaxCreditRelief": true,
        |          "premiumOfLeaseGrantAmount": 100.25,
        |          "otherPropertyIncomeAmount": 100.25,
        |          "foreignTaxPaidOrDeducted": 44.21,
        |          "specialWithholdingTaxOrUKTaxPaid": 23.78
        |        },
        |        "expenses": {
        |          "premisesRunningCostsAmount": 100.25,
        |          "repairsAndMaintenanceAmount": 100.25,
        |          "financialCostsAmount": 200.25,
        |          "professionalFeesAmount": 100.25,
        |          "costOfServicesAmount": 100.25,
        |          "travelCostsAmount": 100.25,
        |          "otherAmount": 100.25
        |         }
        |      }
        |    ]
        |}
      """.stripMargin
    )

    def uri: String = s"/$nino/$businessId/period/$submissionId"

    def ifsUri: String = s"/income-tax/business/property/periodic/$nino/$businessId/$submissionId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }
  }

  "Calling the amend foreign property period summary endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.internalServerError(nino)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "return 201" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.GET, ifsUri, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }
}
