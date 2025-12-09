/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyCumulativeSummary.def2

import common.models.errors.PropertyIdFormatError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.Def2_RetrieveForeignPropertyCumulativeSummaryFixture.*

class Def2_RetrieveForeignPropertyCumulativeSummaryISpec extends IntegrationBaseSpec{

  private trait Test {

    val nino: String       = "AA123456A"
    def taxYear: String    = "2026-27"
    val businessId: String = "XAIS12345678910"
    val propertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

    val responseBody: JsValue = fullMtdJson

    val queryParams = Map("propertyId" -> propertyId)
    def downstreamUri: String = s"/itsa/income-tax/v1/26-27/business/periodic/foreign-property/$nino/$businessId"
    def stubDownstreamSuccess(): Unit =
      DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, queryParams, status = Status.OK, body = fullDownstreamJson)

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/cumulative/$taxYear").addQueryStringParameters("propertyId" -> propertyId)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def setupStubs(): Unit = ()

    def errorBody(code: String): String =
      s"""
         |{
         |   "origin": "HoD",
         |   "response": {
         |      "failures": [
         |         {
         |            "type": "$code",
         |            "reason": "error message"
         |         }
         |      ]
         |   }
         |}
       """.stripMargin

  }

  "Retrieve Foreign property period summary endpoint" should {
    "return a 200 status code" when {
      "successful request is made" in new Test {
        override def setupStubs(): Unit = stubDownstreamSuccess()

        val response: WSResponse = await(request().get())
        response.json shouldBe responseBody
        response.status shouldBe Status.OK
        response.header("X-CorrelationId") should not be empty
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestPropertyId: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear
            override val propertyId: String = requestPropertyId

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678910", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "BAD_TAX_YEAR", "8e8b8450-dc1b-4360-8109-7067337b42cb", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2026-27", "BAD_PROPERTY_ID", BAD_REQUEST, PropertyIdFormatError),
          ("AA123456A", "XAIS12345678910", "2025-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2024-25", "8e8b8450-dc1b-4360-8109-7067337b42cb", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "BAD_BUSINESS_ID", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", Status.BAD_REQUEST, BusinessIdFormatError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }
    }

    "downstream service error" when {
      "return mapped downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, queryParams, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "UNMATCHED_STUB_ERROR", Status.BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "INVALID_PROPERTY_ID", Status.BAD_REQUEST, PropertyIdFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOME_SOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

}
