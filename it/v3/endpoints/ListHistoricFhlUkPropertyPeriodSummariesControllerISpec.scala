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

package v3.endpoints

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class ListHistoricFhlUkPropertyPeriodSummariesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"

    private val fromDate = "2022-08-18"
    private val toDate   = "2022-09-18"
    private val periodId = s"${fromDate}_$toDate"

    val responseBody: JsValue = Json.parse(
      s"""{
         |   "submissions":[
         |      {
         |         "periodId":"$periodId",
         |         "fromDate":"$fromDate",
         |         "toDate":"$toDate",
         |         "links": [
         |           {
         |             "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/$nino/$periodId",
         |             "method": "PUT",
         |             "rel": "amend-uk-property-historic-fhl-period-summary"
         |           },
         |           {
         |             "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/$nino/$periodId",
         |             "method": "GET",
         |             "rel": "self"
         |           }
         |         ]
         |      }
         |   ],
         |  "links": [
         |    {
         |      "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/$nino",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/$nino",
         |      "method": "POST",
         |      "rel": "create-uk-property-historic-fhl-period-summary"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      s"""{
         |  "periods": [
         |    {
         |      "transactionReference": "ignored",
         |      "from": "$fromDate",
         |      "to": "$toDate"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    def ifsUri: String = s"/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/uk/period/furnished-holiday-lettings/$nino")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
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

  "List Historic FHL UK Property Period Summaries endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.json shouldBe responseBody
        response.status shouldBe OK
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino

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

      val input = List(
        ("AA1123A", BAD_REQUEST, NinoFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return ifs service error" when {
      def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
