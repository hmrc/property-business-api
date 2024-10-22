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

package v5.createAmendForeignPropertyCumulativePeriodSummary.def1

import api.models.errors._
import api.models.utils.JsonErrorValidators
import api.services._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class Def1_CreateForeignPropertyPeriodCumulativeSummaryISpec extends IntegrationBaseSpec with JsonErrorValidators {

  private def invalidEntryWithConsolidatedExpenses() =
    Json.parse(s"""
                  |{
                  |    "countryCode": "AFG",
                  |    "expenses": {
                  |        "premisesRunningCosts": 3123.21,
                  |        "consolidatedExpenses": 1.23
                  |    }
                  |}""".stripMargin)

  private def entryWith(countryCode: String, premisesRunningCosts: BigDecimal = 3123.21) =
    Json.parse(s"""
                  |{
                  |    "countryCode": "$countryCode",
                  |    "expenses": {
                  |        "premisesRunningCosts": $premisesRunningCosts
                  |    }
                  |}""".stripMargin)

  private def requestBodyWith(entries: JsValue*) =
    Json.parse(
      s"""{
         |    "fromDate": "2025-01-01",
         |    "toDate": "2026-01-31",
         |    "foreignProperty": ${JsArray(entries)}
         |}
         |""".stripMargin
    )

  private val entry       = entryWith("AFG")
  private val requestBody = requestBodyWith(entry)

  "calling the create endpoint" should {

    "return a 204 status" when {

      "any valid request is made" in new TysIfsTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(method = DownstreamStub.PUT, uri = downstreamUri)
            .thenReturn(status = NO_CONTENT, None)
        }

        val response: WSResponse = await(request().put(requestBody))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
      }
    }

    "return bad request error" when {
      "badly formed json body" in new TysIfsTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }
        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).put("{ badJson }"))
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
          s"validation fails with ${expectedBody.code} error" in new TysIfsTest {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val mtdTaxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }
        val input = List(
          ("AA1123A", "XAIS12345678910", "2025-26", requestBody, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XA***IS1", "2025-26", requestBody, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "20256", requestBody, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2025-27", requestBody, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2024-25", requestBody, BAD_REQUEST, RuleTaxYearNotSupportedError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            requestBodyWith(entryWith("AFG", 1.234)),
            BAD_REQUEST,
            ValueFormatError.forPathAndRange(
              path = "/foreignProperty/0/expenses/premisesRunningCosts",
              min = "-99999999999.99",
              max = "99999999999.99")),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            requestBodyWith(invalidEntryWithConsolidatedExpenses()),
            BAD_REQUEST,
            RuleBothExpensesSuppliedError.copy(paths = Some(List("/foreignProperty/0/expenses")))),
          ("AA123456A", "XAIS12345678910", "2025-26", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "XAIS12345678910", "2025-26", requestBody.update("/fromDate", JsString("XX")), BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "XAIS12345678910", "2025-26", requestBody.update("/toDate", JsString("XX")), BAD_REQUEST, ToDateFormatError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            requestBody.update("/toDate", JsString("1999-01-01")),
            BAD_REQUEST,
            RuleToDateBeforeFromDateError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            requestBodyWith(entryWith("France")),
            BAD_REQUEST,
            CountryCodeFormatError.copy(paths = Some(List("/foreignProperty/0/countryCode")))),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            requestBodyWith(entryWith("QQQ")),
            BAD_REQUEST,
            RuleCountryCodeError.copy(paths = Some(List("/foreignProperty/0/countryCode"))))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new TysIfsTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().put(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOME_SOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "SUBMITTED_TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_SUBMISSION_END_DATE", BAD_REQUEST, RuleAdvanceSubmissionRequiresPeriodEndDateError),
          (UNPROCESSABLE_ENTITY, "SUBMISSION_END_DATE_VALUE", BAD_REQUEST, RuleSubmissionEndDateCannotMoveBackwardsError),
          (UNPROCESSABLE_ENTITY, "INVALID_START_DATE", BAD_REQUEST, RuleStartDateNotAlignedWithReportingTypeError),
          (UNPROCESSABLE_ENTITY, "START_DATE_NOT_ALIGNED", BAD_REQUEST, RuleStartDateNotAlignedToCommencementDateError),
          (UNPROCESSABLE_ENTITY, "END_DATE_NOT_ALIGNED", BAD_REQUEST, RuleEndDateNotAlignedWithReportingTypeError),
          (UNPROCESSABLE_ENTITY, "MISSING_SUBMISSION_DATES", BAD_REQUEST, RuleMissingSubmissionDatesError),
          (UNPROCESSABLE_ENTITY, "START_END_DATE_NOT_ACCEPTED", BAD_REQUEST, RuleStartAndEndDateNotAllowedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (UNPROCESSABLE_ENTITY, "EARLY_DATA_SUBMISSION_NOT_ACCEPTED", BAD_REQUEST, RuleEarlyDataSubmissionNotAcceptedError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {
    val nino: String       = "TC663795B"
    val businessId: String = "XAIS12345678910"

    def mtdTaxYear: String
    def setupStubs(): StubMapping
    def downstreamUri: String

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/cumulative/$mtdTaxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.5.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "ifs message"
         |}
       """.stripMargin

  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String = "2025-26"

    def downstreamUri: String = s"/income-tax/25-26/business/property/periodic/$nino/$businessId"

  }

}
