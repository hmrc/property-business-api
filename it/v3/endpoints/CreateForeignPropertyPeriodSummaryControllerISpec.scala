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
import api.models.utils.JsonErrorValidators
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateForeignPropertyPeriodSummaryControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  private def nonFhlEntryWith(countryCode: String) =
    Json.parse(s"""
         |{
         |    "countryCode": "$countryCode",
         |    "expenses": {
         |        "premisesRunningCosts": 3123.21
         |    }
         |}""".stripMargin)

  private def requestBodyWith(nonFhlEntries: JsValue*) =
    Json.parse(
      s"""{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
      |    "foreignFhlEea": {
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21
      |        }
      |    },
      |    "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
      |}
      |""".stripMargin
    )

  private val nonFhlEntry = nonFhlEntryWith("AFG")
  private val requestBody = requestBodyWith(nonFhlEntry)

  "calling the create endpoint" should {

    "return a 201 status" when {

      "any valid request is made" in new NonTysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().post(requestBody))
        response.json shouldBe mtdResponseBody
        response.status shouldBe CREATED
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().post(requestBody))
        response.json shouldBe mtdResponseBody
        response.status shouldBe CREATED
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return bad request error" when {
      "badly formed json body" in new NonTysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }
        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).post("{ badJson }"))
        response.json shouldBe Json.toJson(BadRequestError)
        response.status shouldBe BAD_REQUEST
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val mtdTaxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678910", "2022-23", requestBody, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "20223", requestBody, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XA***IS1", "2022-23", requestBody, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2021-23", requestBody, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2020-21", requestBody, BAD_REQUEST, RuleTaxYearNotSupportedError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2021-22",
            requestBody.update("/foreignFhlEea/expenses/premisesRunningCosts", JsNumber(1.234)),
            BAD_REQUEST,
            ValueFormatError.copy(paths = Some(List("/foreignFhlEea/expenses/premisesRunningCosts")))),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            requestBody.update("/foreignFhlEea/expenses/consolidatedExpenses", JsNumber(1.23)),
            BAD_REQUEST,
            RuleBothExpensesSuppliedError.copy(paths = Some(List("/foreignFhlEea/expenses")))),
          ("AA123456A", "XAIS12345678910", "2021-22", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "XAIS12345678910", "2022-23", requestBody.update("/fromDate", JsString("XX")), BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", requestBody.update("/toDate", JsString("XX")), BAD_REQUEST, ToDateFormatError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            requestBody.update("/toDate", JsString("1999-01-01")),
            BAD_REQUEST,
            RuleToDateBeforeFromDateError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            requestBodyWith(nonFhlEntryWith("France")),
            BAD_REQUEST,
            CountryCodeFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode")))),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            requestBodyWith(nonFhlEntryWith("QQQ")),
            BAD_REQUEST,
            RuleCountryCodeError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode")))),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            requestBodyWith(nonFhlEntry, nonFhlEntry),
            BAD_REQUEST,
            RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(
              "AFG",
              List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode")))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamQueryParams, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (CONFLICT, "DUPLICATE_SUBMISSION", BAD_REQUEST, RuleDuplicateSubmissionError),
          (UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", BAD_REQUEST, RuleOverlappingPeriodError),
          (UNPROCESSABLE_ENTITY, "GAPS_IN_PERIOD", BAD_REQUEST, RuleNotContiguousPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleToDateBeforeFromDateError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "PERIOD_NOT_ALIGNED", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "PERIOD_OVERLAPS", BAD_REQUEST, RuleOverlappingPeriodError),
          (UNPROCESSABLE_ENTITY, "SUBMISSION_DATE_ISSUE", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "BUSINESS_INCOME_PERIOD_RESTRICTION", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {
    val nino: String       = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val submissionId       = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def mtdTaxYear: String
    def setupStubs(): StubMapping
    def downstreamUri: String
    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/period/$mtdTaxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    val mtdResponseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submissionId": "$submissionId",
         |  "links": [
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$mtdTaxYear/$submissionId",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$mtdTaxYear/$submissionId",
         |      "method":"PUT",
         |      "rel":"amend-foreign-property-period-summary"
         |    }
         |  ]
         |}
      """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submissionId": "$submissionId"
         |}
        """.stripMargin
    )

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "ifs message"
         |}
       """.stripMargin

  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String = "2023-24"

    def downstreamUri: String = "/income-tax/business/property/periodic/23-24"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId
    )

  }

  private trait NonTysTest extends Test {
    def mtdTaxYear: String = "2022-23"

    def downstreamUri: String = "/income-tax/business/property/periodic"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> mtdTaxYear
    )

  }

}
