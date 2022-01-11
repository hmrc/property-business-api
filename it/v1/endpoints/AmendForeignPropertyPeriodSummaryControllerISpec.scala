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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.V1IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, IfsStub, MtdIdLookupStub}

class AmendForeignPropertyPeriodSummaryControllerISpec extends V1IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val businessId: String = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "foreignFhlEea": {
        |    "income": {
        |      "rentAmount": 567.83
        |      },
        |    "expenditure": {
        |      "premisesRunningCosts": 4567.98,
        |      "repairsAndMaintenance": 98765.67,
        |      "financialCosts": 4566.95,
        |      "professionalFees": 23.65,
        |      "costsOfServices": 4567.77,
        |      "travelCosts": 456.77,
        |      "other": 567.67
        |    }
        |  },
        |  "foreignProperty": [{
        |      "countryCode": "FRA",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 34456.30
        |        },
        |        "foreignTaxCreditRelief": true,
        |        "premiumOfLeaseGrant": 2543.43,
        |        "otherPropertyIncome": 54325.30,
        |        "foreignTaxTakenOff": 6543.01,
        |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
        |      },
        |      "expenditure": {
        |        "premisesRunningCosts": 5635.43,
        |        "repairsAndMaintenance": 3456.65,
        |        "financialCosts": 34532.21,
        |        "professionalFees": 32465.32,
        |        "costsOfServices": 2567.21,
        |        "travelCosts": 2345.76,
        |        "residentialFinancialCost": 21235.22,
        |        "broughtFwdResidentialFinancialCost": 12556.00,
        |        "other": 2425.11
        |      }
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/business/property/$nino/$businessId/period/$submissionId",
         |         "method":"PUT",
         |         "rel":"amend-property-period-summary"
         |      },
         |      {
         |         "href":"/individuals/business/property/$nino/$businessId/period/$submissionId",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/business/property/$nino/$businessId/period",
         |         "method":"GET",
         |         "rel":"list-property-period-summaries"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String = s"/$nino/$businessId/period/$submissionId"

    def ifsUri: String = s"/income-tax/business/property/periodic/$nino/$businessId/$submissionId"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin
  }

  "Calling the amend foreign property period summary endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.PUT, ifsUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field validations fail on the request body" in new Test {

        val allInvalidFieldsRequestBodyJson: JsValue = Json.parse(
          s"""
             |{
             |  "foreignFhlEea": {
             |    "income": {
             |      "rentAmount": 567.483
             |      },
             |    "expenditure": {
             |      "consolidatedExpenses": 567.67432
             |    }
             |  },
             |  "foreignProperty": [{
             |      "countryCode": "asgregs",
             |      "income": {
             |        "rentIncome": {
             |          "rentAmount": 34456.31230
             |        },
             |        "foreignTaxCreditRelief": true,
             |        "premiumOfLeaseGrant": 2543.41233,
             |        "otherPropertyIncome": 54325.31230,
             |        "foreignTaxTakenOff": 6543.01231,
             |        "specialWithholdingTaxOrUKTaxPaid": 643245.21300
             |      },
             |      "expenditure": {
             |        "consolidatedExpenses": 2425.11231
             |      }
             |    }
             |  ]
             |}
           """.stripMargin
        )

        val allInvalidFieldsRequestError: List[MtdError] = List(
          CountryCodeFormatError.copy(
            message = "The provided Country code is invalid",
            paths = Some(List(
              "/foreignProperty/0/countryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "One or more monetary fields are invalid",
            paths = Some(List(
              "/foreignFhlEea/income/rentAmount",
              "/foreignFhlEea/expenditure/consolidatedExpenses",
              "/foreignProperty/0/income/rentIncome/rentAmount",
              "/foreignProperty/0/income/premiumOfLeaseGrant",
              "/foreignProperty/0/income/otherPropertyIncome",
              "/foreignProperty/0/income/foreignTaxTakenOff",
              "/foreignProperty/0/income/specialWithholdingTaxOrUKTaxPaid",
              "/foreignProperty/0/expenditure/consolidatedExpenses"
            ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidFieldsRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "return an error according to spec" when {

        val validRequestBodyJson = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "repairsAndMaintenance": 98765.67,
            |      "financialCosts": 4566.95,
            |      "professionalFees": 23.65,
            |      "costsOfServices": 4567.77,
            |      "travelCosts": 456.77,
            |      "other": 567.67
            |    }
            |  },
            |  "foreignProperty": [{
            |      "countryCode": "FRA",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
            |        "premisesRunningCosts": 5635.43,
            |        "repairsAndMaintenance": 3456.65,
            |        "financialCosts": 34532.21,
            |        "professionalFees": 32465.32,
            |        "costsOfServices": 2567.21,
            |        "travelCosts": 2345.76,
            |        "residentialFinancialCost": 21235.22,
            |        "broughtFwdResidentialFinancialCost": 12556.00,
            |        "other": 2425.11
            |      }
            |    }
            |  ]
            |}
          """.stripMargin
        )

        val bothExpensesTypesProvidedJson = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |    "income": {
            |      "rentAmount": 567.83
            |      },
            |    "expenditure": {
            |      "premisesRunningCosts": 4567.98,
            |      "consolidatedExpenses": 456.98
            |    }
            |
            |  },
            |  "foreignProperty": [{
            |      "countryCode": "FRA",
            |      "income": {
            |        "rentIncome": {
            |          "rentAmount": 34456.30
            |        },
            |        "foreignTaxCreditRelief": true,
            |        "premiumOfLeaseGrant": 2543.43,
            |        "otherPropertyIncome": 54325.30,
            |        "foreignTaxTakenOff": 6543.01,
            |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
            |      },
            |      "expenditure": {
            |        "consolidatedExpenses": 352.66
            |      }
            |    }
            |  ]
            |}
          """.stripMargin
        )

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          s"""
             |{
             |  "foreignFhlEea": {
             |    "income": {
             |      "rentAmount": 567.483
             |      },
             |    "expenditure": {
             |      "consolidatedExpenses": 567.67432
             |    }
             |  },
             |  "foreignProperty": [{
             |      "countryCode": "FRA",
             |      "income": {
             |        "rentIncome": {
             |          "rentAmount": 34456.31230
             |        },
             |        "foreignTaxCreditRelief": true,
             |        "premiumOfLeaseGrant": 2543.41233,
             |        "otherPropertyIncome": 54325.31230,
             |        "foreignTaxTakenOff": 6543.01231,
             |        "specialWithholdingTaxOrUKTaxPaid": 643245.21300
             |      },
             |      "expenditure": {
             |        "consolidatedExpenses": 2425.11231
             |      }
             |    }
             |  ]
             |}
           """.stripMargin
        )

        val allInvalidCountryCodeRequestBodyJson: JsValue = Json.parse(
          s"""
             |{
             |  "foreignFhlEea": {
             |    "income": {
             |      "rentAmount": 567.43
             |      },
             |    "expenditure": {
             |      "consolidatedExpenses": 567.67
             |    }
             |  },
             |  "foreignProperty": [{
             |      "countryCode": "asgregs",
             |      "income": {
             |        "rentIncome": {
             |          "rentAmount": 34456.31
             |        },
             |        "foreignTaxCreditRelief": true,
             |        "premiumOfLeaseGrant": 2543.43,
             |        "otherPropertyIncome": 54325.30,
             |        "foreignTaxTakenOff": 6543.01,
             |        "specialWithholdingTaxOrUKTaxPaid": 643245.20
             |      },
             |      "expenditure": {
             |        "consolidatedExpenses": 2425.11
             |      }
             |    }
             |  ]
             |}
           """.stripMargin
        )


        val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
          message = "One or more monetary fields are invalid",
          paths = Some(List(
            "/foreignFhlEea/income/rentAmount",
            "/foreignFhlEea/expenditure/consolidatedExpenses",
            "/foreignProperty/0/income/rentIncome/rentAmount",
            "/foreignProperty/0/income/premiumOfLeaseGrant",
            "/foreignProperty/0/income/otherPropertyIncome",
            "/foreignProperty/0/income/foreignTaxTakenOff",
            "/foreignProperty/0/income/specialWithholdingTaxOrUKTaxPaid",
            "/foreignProperty/0/expenditure/consolidatedExpenses"
          ))
        )

        val allInvalidCountryCodeRequestError: MtdError = CountryCodeFormatError.copy(
          message = "The provided Country code is invalid",
          paths = Some(List(
            "/foreignProperty/0/countryCode"
          ))
        )

        val RuleBothExpensesSuppliedRequestError: MtdError = RuleBothExpensesSuppliedError.copy(
          message = "Both expenses and consolidatedExpenses can not be present at the same time",
          paths = Some(List(
            "/foreignFhlEea/expenditure"
          ))
        )


        "validation error occurs" when {
          def validationErrorTest(requestNino: String,
                                  requestBusinessId: String,
                                  requestSubmissionId: String,
                                  requestBody: JsValue,
                                  expectedStatus: Int,
                                  expectedBody: MtdError): Unit = {
            s"validation fails with ${expectedBody.code} error" in new Test {

              override val nino: String = requestNino
              override val businessId: String = requestBusinessId
              override val submissionId: String = requestSubmissionId
              override val requestBodyJson: JsValue = requestBody

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                AuthStub.authorised()
                MtdIdLookupStub.ninoFound(nino)
              }

              val response: WSResponse = await(request().put(requestBodyJson))
              response.status shouldBe expectedStatus
              response.json shouldBe Json.toJson(expectedBody)
            }
          }

          val input = Seq(
            ("AA1123A", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
            ("AA123456A", "XAIS1234dfxgchjbn5678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError),
            ("AA123456A", "XAIS12345678910", "4557ecb5-fd32-awefwaef48cc-81f5-e6acd1099f3c", validRequestBodyJson, BAD_REQUEST, SubmissionIdFormatError),
            ("AA123456A", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
              Json.parse(s"""{"foreignFhlEea": 2342314}""".stripMargin), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
            ("AA123456A", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", bothExpensesTypesProvidedJson, BAD_REQUEST, RuleBothExpensesSuppliedRequestError),
            ("AA123456A", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError),
            ("AA123456A", "XAIS12345678910", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
              allInvalidCountryCodeRequestBodyJson, BAD_REQUEST, allInvalidCountryCodeRequestError)
          )

          input.foreach(args => (validationErrorTest _).tupled(args))
        }

        "ifs service error" when {
          def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
            s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                AuthStub.authorised()
                MtdIdLookupStub.ninoFound(nino)
                IfsStub.onError(IfsStub.PUT, ifsUri, ifsStatus, errorBody(ifsCode))
              }

              val response: WSResponse = await(request().put(requestBodyJson))
              response.status shouldBe expectedStatus
              response.json shouldBe Json.toJson(expectedBody)
            }
          }

          val input = Seq(
            (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
            (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
            (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
            (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
            (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
            (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
            (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
            (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
            (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

          input.foreach(args => (serviceErrorTest _).tupled(args))
        }
      }
    }
  }
}
