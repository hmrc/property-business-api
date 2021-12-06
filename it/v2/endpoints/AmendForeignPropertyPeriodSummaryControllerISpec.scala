/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.V2IntegrationBaseSpec
import v2.models.errors._
import v2.stubs.{AuditStub, AuthStub, IfsStub, MtdIdLookupStub}

class AmendForeignPropertyPeriodSummaryControllerISpec extends V2IntegrationBaseSpec {

  val requestBodyJson: JsValue = Json.parse(
    """{
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
      |      "other": 3457.9
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
      |        "premisesRunningCosts":129.35,
      |        "repairsAndMaintenance":7490.32,
      |        "financialCosts":5000.99,
      |        "professionalFees":847.90,
      |        "travelCosts":69.20,
      |        "costOfServices":478.23,
      |        "residentialFinancialCost":879.28,
      |        "broughtFwdResidentialFinancialCost":846.13,
      |        "other":138.92
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 334.64
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
      |        "consolidatedExpenses": 3992.93,
      |        "residentialFinancialCost":879.28,
      |        "broughtFwdResidentialFinancialCost":846.13
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  val invalidValueRequestJson: JsValue = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.83459
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 332.73458,
      |      "repairsAndMaintenance": 231.4535,
      |      "financialCosts": -345.25343,
      |      "professionalFees": -232.45,
      |      "costOfServices": 231.5526,
      |      "travelCosts": 234.67253,
      |      "other": 3457.9523
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.3523
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.45238,
      |        "otherPropertyIncome": 802.4359,
      |        "foreignTaxPaidOrDeducted": 734.1328,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.43527
      |      },
      |      "expenses": {
      |        "premisesRunningCosts":129.33525,
      |        "repairsAndMaintenance":7490.3532,
      |        "financialCosts":5000.92359,
      |        "professionalFees":847.93250,
      |        "travelCosts":69.22350,
      |        "costOfServices":478.25323,
      |        "residentialFinancialCost":879.25328,
      |        "broughtFwdResidentialFinancialCost":846.13532,
      |        "other":138.92235
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val bothExpensesSuppliedRequestJson: JsValue = Json.parse(
    """
      |{
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
      |      "other": 3457.9,
      |      "consolidatedExpenses": 3992.93
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
      |        "premisesRunningCosts":129.35,
      |        "repairsAndMaintenance":7490.32,
      |        "financialCosts":5000.99,
      |        "professionalFees":847.90,
      |        "travelCosts":69.20,
      |        "costOfServices":478.23,
      |        "residentialFinancialCost":879.28,
      |        "broughtFwdResidentialFinancialCost":846.13,
      |        "other":138.92,
      |        "consolidatedExpenses": 3992.93
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
    paths = Some(List(

      "/foreignFhlEea/income/rentAmount",
      "/foreignFhlEea/expenses/premisesRunningCosts",
      "/foreignFhlEea/expenses/repairsAndMaintenance",
      "/foreignFhlEea/expenses/financialCosts",
      "/foreignFhlEea/expenses/professionalFees",
      "/foreignFhlEea/expenses/costOfServices",
      "/foreignFhlEea/expenses/travelCosts",
      "/foreignFhlEea/expenses/other",
      "/foreignNonFhlProperty/0/income/rentIncome/rentAmount",
      "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant",
      "/foreignNonFhlProperty/0/income/otherPropertyIncome",
      "/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted",
      "/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid",
      "/foreignNonFhlProperty/0/expenses/premisesRunningCosts",
      "/foreignNonFhlProperty/0/expenses/repairsAndMaintenance",
      "/foreignNonFhlProperty/0/expenses/financialCosts",
      "/foreignNonFhlProperty/0/expenses/professionalFees",
      "/foreignNonFhlProperty/0/expenses/costOfServices",
      "/foreignNonFhlProperty/0/expenses/travelCosts",
      "/foreignNonFhlProperty/0/expenses/residentialFinancialCost",
      "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost",
      "/foreignNonFhlProperty/0/expenses/other",
    ))
  )

  val RuleBothExpensesSuppliedRequestError: MtdError = RuleBothExpensesSuppliedError.copy(
    paths = Some(List(
      "/foreignFhlEea/expenses",
      "/foreignNonFhlProperty/0/expenses"
    ))
  )

  private trait Test {
    val nino: String = "AA123456A"
    val taxYear: String = "2022-23"
    val businessId: String = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def setupStubs(): StubMapping

    def uri: String = s"/foreign/$nino/$businessId/period/$taxYear/$submissionId"

    def ifsUri: String = s"/income-tax/business/property/periodic"

    def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "taxYear" -> taxYear,
      "incomeSourceId" -> businessId,
      "submissionId" -> submissionId
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))
    }

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links": [
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"PUT",
         |      "rel":"amend-foreign-property-period-summary"
         |    },
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/$nino/$businessId/period/$taxYear",
         |      "method":"GET",
         |      "rel":"list-property-period-summaries"
         |    }
         |  ]
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

  "calling the amend foreign property period summary endpoint" should {

    "return a 200 status code" when {

      "any valid unconsolidated request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.PUT, ifsUri, ifsQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid consolidated request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.PUT, ifsUri, ifsQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type").nonEmpty shouldBe true
      }
    }

    "return bad request error" when {
      "field validations fail on the request body" in new Test {
        private val json =
          s"""
             |{
             | badJson
             | }
         """.stripMargin

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).put(json))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(BadRequestError)
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(testName: String,
                              requestNino: String,
                              requestTaxYear: String,
                              requestBusinessId: String,
                              requestSubmissionId: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with $testName error" in new Test {

          override val nino: String = requestNino
          override val taxYear: String = requestTaxYear
          override val businessId: String = requestBusinessId
          override val submissionId: String = requestSubmissionId

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("NINO_FORMAT_ERROR", "AA1123A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, NinoFormatError),
        ("TAX_YEAR_FORMAT_ERROR", "AA123456A", "20223", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
        ("BUSINESSID_FOMAT_ERROR","AA123456A", "2022-23", "XAIS1234dfxgchjbn5678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, BusinessIdFormatError),
        ("SUBMISSIONID_FOMAT_ERROR","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-awefwaef48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, SubmissionIdFormatError),
        ("TAX_YEAR_RANGE_INVALID_ERROR","AA123456A", "2021-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("TAX_YEAR_NOT_SUPPORTED_ERROR","AA123456A", "2020-21", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("RULE_INCORRECT_OR_EMPTY_BODY_ERROR","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse(s"""{}""".stripMargin), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
        ("RULE_INCORRECT_OR_EMPTY_BODY_ERROR with /foreignFhlEea path","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse(s"""{"foreignFhlEea": {}}""".stripMargin), BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignFhlEea")))),
        ("RULE_INCORRECT_OR_EMPTY_BODY_ERROR with /foreignFhlEea/expenses path","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse(s"""{"foreignFhlEea": {"expenses": {}}}""".stripMargin), BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignFhlEea/expenses")))),
        ("VALUE_FOMAT_ERROR","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", invalidValueRequestJson, BAD_REQUEST, allInvalidValueRequestError),
        ("BOTH_EXPENSES_SUPPLIED_ERROR","AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          bothExpensesSuppliedRequestJson, BAD_REQUEST, RuleBothExpensesSuppliedRequestError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return ifs service error" when {
      def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            IfsStub.onError(IfsStub.PUT, ifsUri, ifsQueryParams, ifsStatus, errorBody(ifsCode))
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
        (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_FAILURE", INTERNAL_SERVER_ERROR, DownstreamError),
        (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}


