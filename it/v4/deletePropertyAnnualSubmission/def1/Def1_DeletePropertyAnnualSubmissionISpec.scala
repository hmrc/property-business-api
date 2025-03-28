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

package v4.deletePropertyAnnualSubmission.def1

import shared.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.support.IntegrationBaseSpec
import shared.services._

class Def1_DeletePropertyAnnualSubmissionISpec extends IntegrationBaseSpec {

  "The delete property annual submission endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, downstreamQueryParams, Status.NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made with a Tax Year Specific year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, downstreamQueryParams, Status.NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return a validation error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code}" in new NonTysTest {

            override def nino: String       = requestNino
            override def businessId: String = requestBusinessId
            override def taxYear: String    = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("Walrus", "XAIS12345678910", "2021-22", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2021-22", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "Beans", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2021-23", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", Status.BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "return an MTD error mapped from a downstream error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {

          trait HasTest { _: Test =>
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamErrorCode))
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }

          s"downstream returns $downstreamErrorCode with status $downstreamStatus" in new NonTysTest with HasTest
          s"TYS downstream returns $downstreamErrorCode with status $downstreamStatus" in new TysIfsTest with HasTest
        }

        val errors = List(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    def taxYear: String
    def downstreamUri: String

    def nino: String       = "AA123456A"
    def businessId: String = "XAIS12345678910"

    val mtdUri: String = s"/$nino/$businessId/annual/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def downstreamQueryParams: Map[String, String]

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String           = "2021-22"
    def downstreamTaxYear: String = taxYear
    def downstreamUri: String     = s"/income-tax/business/property/annual"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> downstreamTaxYear
    )

  }

  private trait TysIfsTest extends Test {
    def taxYear: String           = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/business/property/annual/$downstreamTaxYear/$nino/$businessId"

    def downstreamQueryParams: Map[String, String] = Map.empty
  }

}
