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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors.{BusinessIdFormatError, DownstreamError, MtdError, NinoFormatError, NotFoundError, SubmissionIdFormatError, SubmissionIdNotFoundError}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveForeignPropertyPeriodSummaryControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val businessId = "XAIS12345678910"
    val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody = Json.parse(
      s"""
         |{
         |  "fromDate": "2019-04-06",
         |  "toDate": "2019-07-06",
         |  "foreignFhlEea": {
         |    "income": {
         |      "rentAmount": 200.22,
         |      "taxDeducted": 22.22
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
         |          "rentAmount": 200.22,
         |          "taxDeducted": 22.22
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
         |""".stripMargin
    )

    val desResponseBody = Json.parse(
      s"""
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
         |""".stripMargin)

    def uri: String = s"/$nino/$businessId/period/$submissionId"

    def desUri: String = s"/business/property/${nino}/${businessId}/period/${submissionId}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "calling the retrieve endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBody)
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
        def validationErrorTest(requestNino: String, requestBusinessId: String, requestSubmissionId: String,
                                expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
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

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.GET, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "FORMAT_BUSINESS_ID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "FORMAT_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.NOT_FOUND, "SUBMISSION_ID_NOT_FOUND", Status.NOT_FOUND, SubmissionIdNotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
