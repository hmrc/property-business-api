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

package v6.retrieveForeignPropertyAnnualSubmission.def3

import common.models.errors.PropertyIdFormatError
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec
import v6.retrieveForeignPropertyAnnualSubmission.def3.fixture.Def3_RetrieveForeignPropertyAnnualSubmissionFixture.*

class Def3_RetrieveForeignPropertyAnnualSubmissionISpec extends IntegrationBaseSpec {

  "calling the retrieve foreign property annual submission endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = OK,
          body = fullResponseDownstreamJson
        )

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe fullResponseMtdJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
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

        val input: Seq[(String, String, String, String, Int, MtdError)] = List(
          ("AA123", "XAIS12345678910", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2026-27", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2026", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2026-27", "8e8b8450", BAD_REQUEST, PropertyIdFormatError),
          ("AA123456A", "XAIS12345678910", "2026-28", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", "8e8b8450-dc1b-4360-8109-7067337b42cb", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a type $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): Unit = DownstreamStub.onError(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              queryParams = downstreamQueryParams,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors: Seq[(Int, String, Int, MtdError)] = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors: Seq[(Int, String, Int, MtdError)] = List(
          (BAD_REQUEST, "INVALID_PROPERTY_ID", BAD_REQUEST, PropertyIdFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678910"
    val taxYear: String    = "2026-27"
    val propertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

    val downstreamUri: String = s"/itsa/income-tax/v1/26-27/business/foreign-property/annual/$nino/$businessId"

    val downstreamQueryParams: Map[String, String] = Map("propertyId" -> propertyId)

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/annual/$taxYear")
        .addQueryStringParameters("propertyId" -> propertyId)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(`type`: String): String =
      s"""
        |{
        |  "origin": "HoD",
        |  "response": {
        |    "failures": [
        |      {
        |        "type": "${`type`}",
        |        "reason": "message"
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
  }

}
