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

package v6.RetrieveForeignPropertyDetails

import common.models.errors.PropertyIdFormatError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec
import v6.retrieveForeignPropertyDetails.def1.model.Def1_RetrieveForeignPropertyDetailsFixture

class Def1_RetrieveForeignPropertyDetailsHipISpec extends IntegrationBaseSpec with Def1_RetrieveForeignPropertyDetailsFixture {

  private trait Test {

    val nino: String       = "AA123456A"
    def taxYear: String    = "2026-27"
    val businessId: String = "XAIS12345678910"
    val propertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

    val responseBody: JsValue = fullMtdJson
    
     val queryParams = Map("taxYear" -> "26-27", "propertyId" -> "8e8b8450-dc1b-4360-8109-7067337b42cb")
    
    def downstreamUri: String = s"/itsd/income-sources/$nino/foreign-property-details/$businessId"
    
    def stubDownstreamSuccess(): Unit =
      DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, queryParams, status = OK, body = fullDownstreamJson)

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/details/$taxYear").addQueryStringParameters("propertyId" -> "8e8b8450-dc1b-4360-8109-7067337b42cb")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }
    
    def setupStubs(): Unit = ()
    
    def errorBody(code: String): String =
      s"""
           [
         |    {
         |      "errorCode": "$code",
         |      "errorDescription": "message"
         |    }
         |  ]
          """.stripMargin

  }

  "Retrieve Foreign property details endpoint" should {
    "return a 200 status code" when {
      "successful request is made" in new Test {
        override def setupStubs(): Unit = stubDownstreamSuccess()

        val response: WSResponse = await(request().get())
        response.json shouldBe responseBody
        response.status shouldBe OK
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
            
            override def request(): WSRequest = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              setupStubs()
              buildRequest(s"/foreign/$nino/$businessId/details/$taxYear").addQueryStringParameters("propertyId" -> propertyId)
                .withHttpHeaders(
                  (ACCEPT, "application/vnd.hmrc.6.0+json"),
                  (AUTHORIZATION, "Bearer 123")
                )
            }
            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        
        val input = List(
          ("AA1123A", "XAIS12345678910", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "BAD_BUSINESS_ID", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "BAD_TAX_YEAR", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2026-27", "BAD_PROPERTY_ID", BAD_REQUEST, PropertyIdFormatError),
          ("AA123456A", "XAIS12345678910", "2025-26", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XAIS12345678910", "2026-28", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        input.foreach(args => validationErrorTest.tupled(args))
      }
    }

    "downstream service error" when {
      "return mapped downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): Unit = DownstreamStub.onError(
              DownstreamStub.GET,
              downstreamUri,
              queryParams,
              downstreamStatus,
              errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1244", BAD_REQUEST, PropertyIdFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

}
