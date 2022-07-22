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

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.V2IntegrationBaseSpec
import v2.models.errors._
import v2.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class ListPropertyPeriodSummariesControllerISpec extends V2IntegrationBaseSpec {

  private trait Test {

    val nino       = "AA123456A"
    val taxYear    = "2022-23"
    val businessId = "XAIS12345678910"

    private val fromDate     = "2022-08-18"
    private val toDate       = "2022-09-18"
    private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody: JsValue = Json.parse(
      s"""{
         |   "submissions":[
         |      {
         |         "submissionId":"$submissionId",
         |         "fromDate":"$fromDate",
         |         "toDate":"$toDate"
         |      }
         |   ],
         |   "links":[
         |      {
         |         "href":"/individuals/business/property/$nino/$businessId/period/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      s"""[
        |   {
        |      "submissionId": "$submissionId",
        |      "fromDate": "$fromDate",
        |      "toDate": "$toDate"
        |   }
        |]
       """.stripMargin
    )

    def ifsUri: String = s"/income-tax/business/property/$nino/$businessId/period"

    def ifsQueryParams: Map[String, String] = Map(
      "taxYear" -> taxYear
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/$businessId/period/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
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

  "List Property Period Summaries endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.json shouldBe responseBody
        response.status shouldBe Status.OK
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestBusinessId: String,
                              requestTaxYear: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String       = requestNino
          override val businessId: String = requestBusinessId
          override val taxYear: String    = requestTaxYear

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
        ("AA1123A", "XAIS12345678910", "2022-23", Status.BAD_REQUEST, NinoFormatError),
        ("AA123456A", "XAIS12345678910", "20223", Status.BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "XAIS12345678910", "2021-23", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "XAIS12345678910", "2020-21", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "XA123", "2022-23", Status.BAD_REQUEST, BusinessIdFormatError)
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

      val input = Seq(
        (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
        (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
        (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
        (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}