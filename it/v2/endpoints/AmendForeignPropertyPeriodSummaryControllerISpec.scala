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
import v2.stubs.{AuthStub, IfsStub, MtdIdLookupStub}

class AmendForeignPropertyPeriodSummaryControllerISpec extends V2IntegrationBaseSpec {

  private val requestBodyJson = Json.parse(
    """{
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
      |}""".stripMargin)

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
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
      |}""".stripMargin)

  val invalidValueRequestJson = Json.parse(
    """{
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
      |}""".stripMargin)

  val bothExpensesSuppliedRequestJson = Json.parse(
    """{
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
      |}""".stripMargin)

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
      "/foreignNonFhlProperty/0/expenses/other"
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
         |""".stripMargin)

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

      "a valid unconsolidated request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.PUT, ifsUri, ifsQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "a valid consolidated request is made" in new Test {
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
    }

    "return a bad request error" when {
      "field validations fail on the request body" in new Test {
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
        s"validation fails with ${expectedBody.code} error" in new Test {

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
        ("AA1123A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, NinoFormatError),
        ("AA123456A", "20223", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "2022-23", "XAIS1234dfxgchjbn5678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-awefwaef48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, SubmissionIdFormatError),
        ("AA123456A", "2021-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "2020-21", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse(s"""{"foreignFhlEea": {}}""".stripMargin), BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignFhlEea")))),
        ("AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", invalidValueRequestJson, BAD_REQUEST, allInvalidValueRequestError),
        ("AA123456A", "2022-23", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          bothExpensesSuppliedRequestJson, BAD_REQUEST, RuleBothExpensesSuppliedRequestError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }
  }
}
