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

package v4.amendForeignPropertyPeriodSummary.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.{RuleBothExpensesSuppliedError, RuleDuplicateCountryCodeError, RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec

class Def1_AmendForeignPropertyPeriodSummaryISpec extends IntegrationBaseSpec {

  private val requestBodyJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val invalidValueRequestJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.9999
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.9999,
      |      "repairsAndMaintenance": 5000.9999,
      |      "financialCosts": 5000.9999,
      |      "professionalFees": 5000.9999,
      |      "costOfServices": 5000.9999,
      |      "travelCosts": 5000.9999,
      |      "other": 5000.9999
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.9999
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.9999,
      |        "otherPropertyIncome": 5000.9999,
      |        "foreignTaxPaidOrDeducted": 5000.9999,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.9999
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.9999,
      |        "repairsAndMaintenance": 5000.9999,
      |        "financialCosts": 5000.9999,
      |        "professionalFees": 5000.9999,
      |        "costOfServices": 5000.9999,
      |        "travelCosts": 5000.9999,
      |        "residentialFinancialCost": 5000.9999,
      |        "broughtFwdResidentialFinancialCost": 5000.9999,
      |        "other": 5000.9999
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val bothExpensesSuppliedRequestJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val duplicateCountryCodeRequestJson = Json.parse(
    """
      |{
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "expenses": {
      |        "repairsAndMaintenance": 5000.99
      |      }
      |    },
      |    {
      |      "countryCode": "FRA",
      |      "expenses": {
      |        "repairsAndMaintenance": 5000.99
      |      }
      |    }
      |
      |  ]
      |}
    """.stripMargin
  )

  private def invalidCountryCodeRequestJson(countryCode: String): JsValue = Json.parse(
    s"""
      |{
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "$countryCode",
      |      "expenses": {
      |        "repairsAndMaintenance": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
    paths = Some(
      List(
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
        "/foreignNonFhlProperty/0/expenses/other"
      ))
  )

  val ruleBothExpensesSuppliedRequestError: MtdError = RuleBothExpensesSuppliedError.copy(
    paths = Some(
      List(
        "/foreignFhlEea/expenses",
        "/foreignNonFhlProperty/0/expenses"
      ))
  )

  val ruleDuplicateCountryCodeRequestError: MtdError = RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(
    code = "FRA",
    paths = List(
      "/foreignNonFhlProperty/0/countryCode",
      "/foreignNonFhlProperty/1/countryCode"
    )
  )

  private trait Test {
    val nino: String         = "AA123456A"
    val businessId: String   = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def taxYear: String
    def downstreamTaxYear: String
    def downstreamUri: String

    def setupStubs(): StubMapping

    def uri: String = s"/foreign/$nino/$businessId/period/$taxYear/$submissionId"

    def baseUri: String = s"/income-tax/business/property/periodic"

    def commonQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "submissionId"    -> submissionId
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "Downstream message"
         |}
      """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String                            = "2022-23"
    def downstreamTaxYear: String                  = "2022-23"
    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> downstreamTaxYear) ++ commonQueryParams

    override def downstreamUri: String = baseUri
  }

  private trait TysIfsTest extends Test {
    def taxYear: String                            = "2023-24"
    def downstreamTaxYear: String                  = "23-24"
    def downstreamQueryParams: Map[String, String] = commonQueryParams

    override def downstreamUri: String = baseUri + s"/$downstreamTaxYear"
  }

  "calling the amend foreign property period summary endpoint" should {

    "return a 200 status code" when {

      "a valid unconsolidated request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "a valid unconsolidated request is made with a Tax Year Specific tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "a valid consolidated request is made" in new NonTysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "a valid consolidated request is made with a Tax Year Specific tax year" in new TysIfsTest {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        MtdIdLookupStub.ninoFound(nino)
        DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
      }

      val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
      response.status shouldBe OK
      response.body shouldBe ""
      response.header("X-CorrelationId").nonEmpty shouldBe true
    }
  }

  "return a bad request error" when {
    "field validations fail on the request body" in new NonTysTest {
      private val json =
        s"""
             |{
             |  badJson
             |}
             |""".stripMargin

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
    def validationErrorTest(requestNino: String,
                            requestTaxYear: String,
                            requestBusinessId: String,
                            requestSubmissionId: String,
                            requestBody: JsValue,
                            expectedStatus: Int,
                            expectedBody: MtdError): Unit = {
      s"validation fails with ${expectedBody.code} error" in new NonTysTest {

        override val nino: String         = requestNino
        override val taxYear: String      = requestTaxYear
        override val businessId: String   = requestBusinessId
        override val submissionId: String = requestSubmissionId

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(requestBody))
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }

    val input = List(
      ("AA1123A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, NinoFormatError),
      ("AA123456A", "20223", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
      (
        "AA123456A",
        "2022-23",
        "XAIS1234dfxgchjbn5678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        requestBodyJson,
        BAD_REQUEST,
        BusinessIdFormatError),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-awefwaef48cc-81f5-e6acd1099f3c",
        requestBodyJson,
        BAD_REQUEST,
        SubmissionIdFormatError),
      ("AA123456A", "2021-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
      ("AA123456A", "2020-21", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        Json.parse(s"""{"foreignFhlEea": {}}""".stripMargin),
        BAD_REQUEST,
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/foreignFhlEea")))),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        invalidValueRequestJson,
        BAD_REQUEST,
        allInvalidValueRequestError),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        bothExpensesSuppliedRequestJson,
        BAD_REQUEST,
        ruleBothExpensesSuppliedRequestError),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        duplicateCountryCodeRequestJson,
        BAD_REQUEST,
        ruleDuplicateCountryCodeRequestError),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        invalidCountryCodeRequestJson("FRANCE"),
        BAD_REQUEST,
        CountryCodeFormatError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode")))),
      (
        "AA123456A",
        "2022-23",
        "XAIS12345678910",
        "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        invalidCountryCodeRequestJson("SBT"),
        BAD_REQUEST,
        RuleCountryCodeError.copy(paths = Some(List("/foreignNonFhlProperty/0/countryCode"))))
    )
    input.foreach(args => (validationErrorTest _).tupled(args))
  }

  "return downstream service error" when {
    def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
      s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }

    val errors = List(
      (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
      (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
      (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
      (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
      (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
      (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
      (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
      (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_FAILURE", INTERNAL_SERVER_ERROR, InternalError),
      (UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
      (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
      (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
      (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
    )

    val extraTysErrors = List(
      (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
      (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INCOME_SOURCE_NOT_COMPATIBLE", BAD_REQUEST, RuleTypeOfBusinessIncorrectError)
    )

    (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
  }

}
