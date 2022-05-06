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
import v2.stubs.{AuditStub, AuthStub, IfsStub, MtdIdLookupStub}

class RetrieveForeignPropertyPeriodSummaryControllerISpec extends V2IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2022-23"
    val businessId: String = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submittedOn": "2021-06-17T10:53:38Z",
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
         |  ],
         |  "links": [
         |    {
         |      "href": "/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method": "PUT",
         |      "rel": "amend-foreign-property-period-summary"
         |    },
         |    {
         |      "href": "/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/$nino/$businessId/period/$taxYear",
         |      "method": "GET",
         |      "rel": "list-property-period-summaries"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2021-06-17T10:53:38Z",
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

    def uri: String = s"/foreign/$nino/$businessId/period/$taxYear/$submissionId"

    def ifsUri: String = s"/income-tax/business/property/periodic"

    def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "taxYear" -> taxYear,
      "incomeSourceId" -> businessId,
      "submissionId" -> submissionId
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
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

  "Retrieve Foreign property period summary endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.GET, ifsUri, ifsQueryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String, requestBusinessId: String,
                              requestTaxYear: String, requestSubmissionId: String,
                              expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino
          override val businessId: String = requestBusinessId
          override val submissionId: String = requestSubmissionId
          override val taxYear: String = requestTaxYear

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
        ("AA1123A", "XAIS12345678910", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, NinoFormatError),
        ("AA123456A", "XA123", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "XAIS12345678910", "2022-23", "4557ecb5-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, SubmissionIdFormatError),
        ("AA123456A", "XAIS12345678910", "20223", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "XAIS12345678910", "2021-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "XAIS12345678910", "2020-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, RuleTaxYearNotSupportedError)
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
            IfsStub.onError(IfsStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
        (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
        (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
        (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
        (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
        (Status.BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
        (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}