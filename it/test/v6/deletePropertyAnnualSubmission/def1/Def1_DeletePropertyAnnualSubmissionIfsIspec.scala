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

package v6.deletePropertyAnnualSubmission.def1

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.RuleOutsideAmendmentWindowError
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*

class Def1_DeletePropertyAnnualSubmissionIfsIspec extends IntegrationBaseSpec {
  override def servicesConfig: Map[String, Any] = Map("feature-switch.ifs_hip_migration_1863.enabled" -> false) ++ super.servicesConfig

  "calling the delete property annual submission endpoint" should {
    "return a 204 status code" when {
      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysTest {
        override def setupStubs(): StubMapping = DownstreamStub.onSuccess(
          method = DownstreamStub.DELETE,
          uri = downstreamUri,
          status = NO_CONTENT
        )

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made" in new NonTysTest {
        override def setupStubs(): StubMapping = DownstreamStub.onSuccess(
          method = DownstreamStub.DELETE,
          queryParams = downstreamQueryParams,
          uri = downstreamUri,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"Non-Tys validation fails with ${expectedBody.code} error" in new NonTysTest with Test {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }

          s"validation fails with ${expectedBody.code} error" in new TysTest with Test {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "XAIS12345678910", "2025-26", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid-business-id", "2025-26", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "20256", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XAIS12345678910", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new TysTest with Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().delete())
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
            response.header("X-CorrelationId").nonEmpty shouldBe true
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }

  }

  private trait Test {
    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678910"

    def taxYear: String
    def downstreamUri: String

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/$businessId/annual/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String = {
      s"""
         |{
         |  "failures": [
         |    {
         |      "code": "${code}",
         |      "reason": "message"
         |    }
         |  ]
         |}
      """.stripMargin
    }

  }

  private trait TysTest extends Test {
    def taxYear: String       = "2025-26"
    def downstreamUri: String = s"/income-tax/business/property/annual/25-26/$nino/$businessId"
  }

  private trait NonTysTest extends Test {
    def taxYear: String       = "2022-23"
    def downstreamUri: String = s"/income-tax/business/property/annual"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> "2022-23"
    )

  }

}
