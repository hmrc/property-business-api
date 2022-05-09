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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.V1IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, IfsStub, MtdIdLookupStub}

class ListForeignPropertiesPeriodSummariesControllerISpec extends V1IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val businessId: String = "XAIS12345678910"
    val fromDate: String = "2020-05-22"
    val toDate: String = "2020-09-22"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submissions": [
         |    {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |      "fromDate": "2020-06-22",
         |      "toDate": "2020-06-22",
         |      "links": [
         |        {
         |          "href": "/individuals/business/property/$nino/$businessId/period/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |          "method": "GET",
         |          "rel": "self"
         |        }
         |      ]
         |    },
         |    {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
         |      "fromDate": "2020-08-22",
         |      "toDate": "2020-08-22",
         |      "links": [
         |        {
         |          "href": "/individuals/business/property/$nino/$businessId/period/4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
         |          "method": "GET",
         |          "rel": "self"
         |        }
         |      ]
         |    }
         |  ],
         |  "links": [
         |    {
         |      "href": "/individuals/business/property/$nino/$businessId/period",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/$nino/$businessId/period",
         |      "method": "POST",
         |      "rel": "create-property-period-summary"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
       """
         |[
         |  {
         |    "submittedOn": "2020-06-22T22:00:20Z",
         |    "foreignProperty": {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |      "fromDate": "2020-06-22",
         |      "toDate": "2020-06-22"
         |    },
         |    "foreignFhlEea": {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |      "fromDate": "2020-06-22",
         |      "toDate": "2020-06-22"
         |    }
         |  },
         |  {
         |    "submittedOn": "2020-08-22T22:00:20Z",
         |    "foreignFhlEea": {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
         |      "fromDate": "2020-08-22",
         |      "toDate": "2020-08-22"
         |    }
         |  }
         |]
       """.stripMargin
    )

    def uri: String = s"/$nino/$businessId/period"

    def queryParams: Map[String, String] = Map(
      "fromDate" -> fromDate,
      "toDate" -> toDate
    )

    def ifsUri: String = s"/income-tax/business/property/${nino}/${businessId}/period"

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

  "calling the list foreign properties period summary endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.GET, ifsUri, queryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().withQueryStringParameters("fromDate" -> fromDate, "toDate" -> toDate).get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(
                                 requestNino: String,
                                 requestBusinessId: String,
                                 requestFromDate: String,
                                 requestToDate: String,
                                 expectedStatus: Int,
                                 expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val fromDate: String = requestFromDate
            override val toDate: String = requestToDate

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().withQueryStringParameters("fromDate" -> fromDate, "toDate" -> toDate).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        "validation fails with missing to date error" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound("AA123456A")
          }

          val response: WSResponse = await(request().withQueryStringParameters("fromDate" -> "2020-06-22").get())
          response.status shouldBe Status.BAD_REQUEST
          response.json shouldBe Json.toJson(MissingToDateError)
        }

        "validation fails with missing from date error" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound("AA123456A")
          }

          val response: WSResponse = await(request().withQueryStringParameters("toDate" -> "2020-06-22").get())
          response.status shouldBe Status.BAD_REQUEST
          response.json shouldBe Json.toJson(MissingFromDateError)
        }

        val input = Seq(
          ("Walrus", "XAIS12345678910", "2020-05-22", "2020-09-22", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2020-05-22", "2020-09-22", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "20-20-2", "2020-09-22", Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "XAIS12345678910", "2020-05-22", "20-2-02", Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "XAIS12345678910", "2020-05-22", "2020-04-22", Status.BAD_REQUEST, RuleToDateBeforeFromDateError)
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
              IfsStub.onError(IfsStub.GET, ifsUri, queryParams, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().withQueryStringParameters("fromDate" -> fromDate, "toDate" -> toDate).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.BAD_REQUEST, "INVALID_FROM_DATE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_TO_DATE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_DATE_REQUEST", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}