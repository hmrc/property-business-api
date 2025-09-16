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

package v4.retrieveForeignPropertyPeriodSummary.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.SubmissionIdFormatError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec

class RetrieveForeignPropertyPeriodSummaryISpec extends IntegrationBaseSpec {

  "Retrieve Foreign property period summary endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with a Tax Year Specific year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestBusinessId: String,
                              requestTaxYear: String,
                              requestSubmissionId: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code}" in new NonTysTest {

          override def nino: String         = requestNino
          override def businessId: String   = requestBusinessId
          override def submissionId: String = requestSubmissionId
          override def taxYear: String      = requestTaxYear

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
        ("AA1123A", "XAIS12345678910", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "XA123", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "XAIS12345678910", "2022-23", "4557ecb5-48cc-81f5-e6acd1099f3c", BAD_REQUEST, SubmissionIdFormatError),
        ("AA123456A", "XAIS12345678910", "20223", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "XAIS12345678910", "2021-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "XAIS12345678910", "2020-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )
      input.foreach(args => (validationErrorTest).tupled(args))
    }

    "return an MTD error mapped from a downstream error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {

        trait HasTest { self: Test =>
          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamErrorCode))
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }

        s"downstream returns $downstreamErrorCode with status $downstreamStatus" in new NonTysTest with HasTest
        s"TYS downstream returns $downstreamErrorCode with status $downstreamStatus" in new TysIfsTest with HasTest
      }

      val errors = List(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = List(
        (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceErrorTest).tupled(args))
    }
  }

  private trait Test {

    def taxYear: String
    def downstreamUri: String
    def downstreamQueryParams: Map[String, String]

    def nino: String         = "AA123456A"
    def businessId: String   = "XAIS12345678910"
    def submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submittedOn": "2021-06-17T10:53:38.000Z",
         |  "fromDate": "2020-01-29",
         |  "toDate": "2020-04-29",
         |  "foreignFhlEea": {
         |    "income": {
         |      "rentAmount": 1123.89
         |    },
         |    "expenses": {
         |      "premisesRunningCosts": 332.78,
         |      "repairsAndMaintenance": 231.45,
         |      "financialCosts": 345.23,
         |      "professionalFees": 232.45,
         |      "costOfServices": 231.56,
         |      "travelCosts": 234.67,
         |      "other": 3457.90
         |    }
         |  },
         |  "foreignNonFhlProperty": [
         |    {
         |      "countryCode": "AFG",
         |      "income": {
         |        "rentIncome": {
         |          "rentAmount": 440.31
         |        },
         |        "foreignTaxCreditRelief": false,
         |        "premiumsOfLeaseGrant": 950.48,
         |        "otherPropertyIncome": 802.49,
         |        "foreignTaxPaidOrDeducted": 734.18,
         |        "specialWithholdingTaxOrUkTaxPaid": 85.47
         |      },
         |      "expenses": {
         |        "premisesRunningCosts": 4929.50,
         |        "repairsAndMaintenance": 54.30,
         |        "financialCosts": 2090.35,
         |        "professionalFees": 90.20,
         |        "travelCosts": 560.99,
         |        "costOfServices": 100.83,
         |        "residentialFinancialCost": 857.78,
         |        "broughtFwdResidentialFinancialCost": 600.10,
         |        "other": 334.64
         |      }
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2021-06-17T10:53:38.000Z",
        |  "fromDate": "2020-01-29",
        |  "toDate": "2020-04-29",
        |  "foreignFhlEea": {
        |    "income": {
        |      "rentAmount": 1123.89
        |    },
        |    "expenses": {
        |      "premisesRunningCosts": 332.78,
        |      "repairsAndMaintenance": 231.45,
        |      "financialCosts": 345.23,
        |      "professionalFees": 232.45,
        |      "costOfServices": 231.56,
        |      "travelCosts": 234.67,
        |      "other": 3457.90
        |    }
        |  },
        |  "foreignProperty": [
        |    {
        |      "countryCode": "AFG",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 440.31
        |        },
        |        "foreignTaxCreditRelief": false,
        |        "premiumsOfLeaseGrant": 950.48,
        |        "otherPropertyIncome": 802.49,
        |        "foreignTaxPaidOrDeducted": 734.18,
        |        "specialWithholdingTaxOrUkTaxPaid": 85.47
        |      },
        |      "expenses": {
        |        "premisesRunningCosts": 4929.50,
        |        "repairsAndMaintenance": 54.30,
        |        "financialCosts": 2090.35,
        |        "professionalFees": 90.20,
        |        "travelCosts": 560.99,
        |        "costOfServices": 100.83,
        |        "residentialFinancialCost": 857.78,
        |        "broughtFwdResidentialFinancialCost": 600.10,
        |        "other": 334.64
        |      }
        |    }
        |  ]
        |}
       """.stripMargin
    )

    private val mtdUri: String = s"/foreign/$nino/$businessId/period/$taxYear/$submissionId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
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

  private trait NonTysTest extends Test {
    def taxYear: String           = "2022-23"
    private def downstreamTaxYear: String = taxYear
    def downstreamUri: String     = s"/income-tax/business/property/periodic"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "taxYear"         -> downstreamTaxYear,
      "incomeSourceId"  -> businessId,
      "submissionId"    -> submissionId
    )

  }

  private trait TysIfsTest extends Test {
    def taxYear: String           = "2023-24"
    private def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/business/property/$downstreamTaxYear/$nino/$businessId/periodic/$submissionId"

    def downstreamQueryParams: Map[String, String] = Map.empty
  }

}
