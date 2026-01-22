/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.retrieveUkPropertyPeriodSummary.def2

import common.models.errors.RuleTypeOfBusinessIncorrectError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.models.domain.TaxYear
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec
import v6.retrieveUkPropertyPeriodSummary.def2.model.Def2_RetrieveUkPropertyPeriodSummaryFixture


class Def2_RetrieveUkPropertyPeriodSummaryHipISpec extends IntegrationBaseSpec with Def2_RetrieveUkPropertyPeriodSummaryFixture  {

  private trait Test {

    val nino: String = "AA123456A"

    def taxYear: String = "2024-25"

    val businessId: String = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def downstreamQueryParams: Map[String, String] = Map("submissionId" -> submissionId)

    val responseBody: JsValue = fullMtdJson

    def downstreamUri: String = s"/itsa/income-tax/v1/${TaxYear.fromMtd(taxYear).asTysDownstream}/business/property/periodic/$nino/$businessId"

    def stubDownstreamSuccess(): Unit =
      DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, status = Status.OK, body = fullDownstreamJson)

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/uk/$nino/$businessId/period/$taxYear/$submissionId")
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

  "Retrieve UK property period summary endpoint" should {
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

    "return a 400 status code with RULE_TYPE_OF_BUSINESS_INCORRECT error" when {
      "downstream returns a non uk result" in new Test {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(
            DownstreamStub.GET,
            downstreamUri,
            status = Status.OK,
            body = Json.parse(
              """{
                |  "submittedOn": "2025-06-17T10:53:38.000Z",
                |  "fromDate": "2024-01-29",
                |  "toDate": "2025-03-29",
                |  "ukFhlProperty": { },
                |  "ukOtherProperty": { }
                |}""".stripMargin)
          )

          val response: WSResponse = await(request().get())
          response.json shouldBe Json.toJson(RuleTypeOfBusinessIncorrectError)
          response.status shouldBe Status.BAD_REQUEST
      }
    }

    "return validation error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678910", "2025-26", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "BAD_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2025-27", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2026-27", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "BAD_BUSINESS_ID", "2023-24", Status.BAD_REQUEST, BusinessIdFormatError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }
    }

    "downstream service error" when {
      "return mapped downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "UNMATCHED_STUB_ERROR", Status.BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))
      }
    }

  }
}
