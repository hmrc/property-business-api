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

import shared.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.{RuleBothExpensesSuppliedError, RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.support.IntegrationBaseSpec
import shared.services._

class AmendUkPropertyPeriodSummaryControllerISpec extends IntegrationBaseSpec {

  val requestBodyJson: JsValue = Json.parse(
    """{
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "travelCosts": 974.47,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    }
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    }
      |}
      |""".stripMargin
  )

  val invalidValueRequestJson: JsValue = Json.parse(
    """
      |{
      |  "ukFhlProperty": {
      |    "income": {
      |            "periodAmount": 5000.99634874383248236385,
      |            "taxDeducted": 5000.99634874383248236385,
      |            "rentARoom": {
      |                "rentsReceived": 5000.99634874383248236385
      |            }
      |        },
      |    "expenses": {
      |            "premisesRunningCosts": 5000.99634874383248236385,
      |            "repairsAndMaintenance": 5000.99634874383248236385,
      |            "financialCosts": 5000.99634874383248236385,
      |            "professionalFees": 5000.99634874383248236385,
      |            "costOfServices": 5000.99634874383248236385,
      |            "other": 5000.99634874383248236385,
      |            "travelCosts": 5000.99634874383248236385,
      |            "rentARoom": {
      |                "amountClaimed": 5000.99634874383248236385
      |            }
      |        }
      |  },
      |  "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 5000.99634874383248236385,
      |            "reversePremiums": 5000.99634874383248236385,
      |            "periodAmount": 5000.99634874383248236385,
      |            "taxDeducted": 5000.99634874383248236385,
      |            "otherIncome": 5000.99634874383248236385,
      |            "rentARoom": {
      |                "rentsReceived": 5000.99634874383248236385
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 5000.99634874383248236385,
      |            "repairsAndMaintenance": 5000.99634874383248236385,
      |            "financialCosts": 5000.99634874383248236385,
      |            "professionalFees": 5000.99634874383248236385,
      |            "costOfServices": 5000.99634874383248236385,
      |            "other": 5000.99634874383248236385,
      |            "residentialFinancialCost": 5000.99634874383248236385,
      |            "travelCosts": 5000.99634874383248236385,
      |            "residentialFinancialCostsCarriedForward": 5000.99634874383248236385,
      |            "rentARoom": {
      |                "amountClaimed": 5000.99634874383248236385
      |            }
      |        }
      |    }
      |}
    """.stripMargin
  )

  val bothExpensesSuppliedRequestJson: JsValue = Json.parse(
    """
      |{
      |  "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "travelCosts": 974.47,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            },
      |            "consolidatedExpenses": 5000.99
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            },
      |            "consolidatedExpenses": 5000.99
      |        }
      |    }
      |}
    """.stripMargin
  )

  val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
    paths = Some(
      List(
        "/ukFhlProperty/income/periodAmount",
        "/ukFhlProperty/income/taxDeducted",
        "/ukFhlProperty/income/rentARoom/rentsReceived",
        "/ukFhlProperty/expenses/premisesRunningCosts",
        "/ukFhlProperty/expenses/repairsAndMaintenance",
        "/ukFhlProperty/expenses/financialCosts",
        "/ukFhlProperty/expenses/professionalFees",
        "/ukFhlProperty/expenses/costOfServices",
        "/ukFhlProperty/expenses/other",
        "/ukFhlProperty/expenses/travelCosts",
        "/ukFhlProperty/expenses/rentARoom/amountClaimed",
        "/ukNonFhlProperty/income/premiumsOfLeaseGrant",
        "/ukNonFhlProperty/income/reversePremiums",
        "/ukNonFhlProperty/income/periodAmount",
        "/ukNonFhlProperty/income/taxDeducted",
        "/ukNonFhlProperty/income/otherIncome",
        "/ukNonFhlProperty/income/rentARoom/rentsReceived",
        "/ukNonFhlProperty/expenses/premisesRunningCosts",
        "/ukNonFhlProperty/expenses/repairsAndMaintenance",
        "/ukNonFhlProperty/expenses/financialCosts",
        "/ukNonFhlProperty/expenses/professionalFees",
        "/ukNonFhlProperty/expenses/costOfServices",
        "/ukNonFhlProperty/expenses/other",
        "/ukNonFhlProperty/expenses/residentialFinancialCost",
        "/ukNonFhlProperty/expenses/travelCosts",
        "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward",
        "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"
      ))
  )

  val RuleBothExpensesSuppliedRequestError: MtdError = RuleBothExpensesSuppliedError.copy(
    paths = Some(
      List(
        "/ukFhlProperty/expenses",
        "/ukNonFhlProperty/expenses"
      ))
  )

  private trait Test {
    val nino: String         = "AA123456A"
    val businessId: String   = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def taxYear: String
    def downstreamTaxYear: String

    def setupStubs(): StubMapping

    def uri: String = s"/uk/$nino/$businessId/period/$taxYear/$submissionId"

    def baseUri: String = s"/income-tax/business/property/periodic"
    def downstreamUri: String

    def commonQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "submissionId"    -> submissionId
    )

    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links": [
         |    {
         |      "href":"/individuals/business/property/$nino/$businessId/period/$taxYear",
         |      "method":"GET",
         |      "rel":"list-property-period-summaries"
         |    },
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"PUT",
         |      "rel":"amend-uk-property-period-summary"
         |    }
         |  ]
         |}
     """.stripMargin
    )

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "downstream message"
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

  "calling the amend uk property period summary endpoint" should {

    "return a 200 status code" when {

      "any valid unconsolidated request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid unconsolidated request is made with a Tax Year Specific tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid consolidated request is made" in new NonTysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type").nonEmpty shouldBe true
      }

      "any valid consolidated request is made with a Tax Year Specific tax year" in new TysIfsTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type").nonEmpty shouldBe true
      }
    }

    "return bad request error" when {
      "field validations fail on the request body" in new NonTysTest {
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
        (
          "AA123456A",
          "2021-23",
          "XAIS12345678910",
          "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          requestBodyJson,
          BAD_REQUEST,
          RuleTaxYearRangeInvalidError),
        (
          "AA123456A",
          "2021-22",
          "XAIS12345678910",
          "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          requestBodyJson,
          BAD_REQUEST,
          RuleTaxYearNotSupportedError),
        (
          "AA123456A",
          "2022-23",
          "XAIS12345678910",
          "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          Json.parse(s"""{"ukFhlProperty": {}}""".stripMargin),
          BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/ukFhlProperty")))),
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
          RuleBothExpensesSuppliedRequestError)
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
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
        (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", INTERNAL_SERVER_ERROR, InternalError),
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

}
