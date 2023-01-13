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
import v1.models.errors.{ BusinessIdFormatError, DownstreamError, MtdError, NinoFormatError, NotFoundError, SubmissionIdFormatError }
import v1.stubs.{ AuditStub, AuthStub, IfsStub, MtdIdLookupStub }

class RetrieveForeignPropertyPeriodSummaryControllerISpec extends V1IntegrationBaseSpec {

  private trait Test {

    val nino: String         = "AA123456A"
    val businessId: String   = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "fromDate": "2019-04-06",
         |  "toDate": "2019-07-06",
         |  "foreignFhlEea": {
         |    "income": {
         |      "rentAmount": 200.22
         |    },
         |    "expenditure": {
         |      "premisesRunningCosts": 100.25,
         |      "repairsAndMaintenance": 100.25,
         |      "financialCosts": 100.25,
         |      "professionalFees": 100.25,
         |      "costsOfServices": 100.25,
         |      "travelCosts": 100.25,
         |      "other": 100.25
         |    }
         |  },
         |  "foreignProperty": [
         |    {
         |      "countryCode": "FRA",
         |      "income": {
         |        "rentIncome": {
         |          "rentAmount": 200.22
         |        },
         |        "foreignTaxCreditRelief": true,
         |        "premiumOfLeaseGrant": 100.25,
         |        "otherPropertyIncome": 100.25,
         |        "foreignTaxTakenOff": 44.21,
         |        "specialWithholdingTaxOrUKTaxPaid": 23.78
         |      },
         |      "expenditure": {
         |        "premisesRunningCosts": 100.25,
         |        "repairsAndMaintenance": 100.25,
         |        "financialCosts": 200.25,
         |        "professionalFees": 100.25,
         |        "costsOfServices": 100.25,
         |        "travelCosts": 100.25,
         |        "other": 100.25
         |      }
         |    }
         |  ],
         |  "links": [
         |    {
         |      "href": "/individuals/business/property/${nino}/${businessId}/period/${submissionId}",
         |      "method": "PUT",
         |      "rel": "amend-property-period-summary"
         |    },
         |    {
         |      "href": "/individuals/business/property/${nino}/${businessId}/period/${submissionId}",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/${nino}/${businessId}/period",
         |      "method": "GET",
         |      "rel": "list-property-period-summaries"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      """
         |{
         |  "fromDate": "2019-04-06",
         |  "toDate": "2019-07-06",
         |  "foreignFhlEea": {
         |        "income": {
         |          "rentAmount": 200.22
         |        },
         |        "expenses": {
         |          "premisesRunningCosts": 100.25,
         |          "repairsAndMaintenance": 100.25,
         |          "financialCosts": 100.25,
         |          "professionalFees": 100.25,
         |          "costOfServices": 100.25,
         |          "travelCosts": 100.25,
         |          "other": 100.25
         |        }
         |      },
         |  "foreignProperty": [
         |      {
         |        "countryCode": "FRA",
         |        "income": {
         |            "rentIncome": {
         |                "rentAmount": 200.22
         |            },
         |          "foreignTaxCreditRelief": true,
         |          "premiumsOfLeaseGrant": 100.25,
         |          "otherPropertyIncome": 100.25,
         |          "foreignTaxPaidOrDeducted": 44.21,
         |          "specialWithholdingTaxOrUkTaxPaid": 23.78
         |        },
         |        "expenses": {
         |          "premisesRunningCosts": 100.25,
         |          "repairsAndMaintenance": 100.25,
         |          "financialCosts": 200.25,
         |          "professionalFees": 100.25,
         |          "costOfServices": 100.25,
         |          "travelCosts": 100.25,
         |          "other": 100.25
         |         }
         |      }
         |    ]
         |}
       """.stripMargin
    )

    def uri: String = s"/$nino/$businessId/period/$submissionId"

    def ifsUri: String = s"/income-tax/business/property/periodic/${nino}/${businessId}/${submissionId}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin
  }

  "calling the retrieve foreign property period summary endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.GET, ifsUri, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestSubmissionId: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String         = requestNino
            override val businessId: String   = requestBusinessId
            override val submissionId: String = requestSubmissionId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("Walrus", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "Beans", Status.BAD_REQUEST, SubmissionIdFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              IfsStub.onError(IfsStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
