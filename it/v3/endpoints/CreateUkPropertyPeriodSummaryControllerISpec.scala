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
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateUkPropertyPeriodSummaryControllerISpec extends IntegrationBaseSpec {

  private val requestBodyJson = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
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

  private val requestBodyJsonConsolidatedExpense = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
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

  val invalidToDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "20190406",
      |  "ukFhlProperty":{
      |    "expenses": {
      |       "consolidatedExpenses": 988.18
      |    }
      |  }
      |}
    """.stripMargin
  )

  val invalidFromDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "20180406",
      |  "toDate": "2019-04-06",
      |  "ukFhlProperty":{
      |    "expenses": {
      |       "consolidatedExpenses": 988.18
      |    }
      |  }
      |}
    """.stripMargin
  )

  val invalidValueRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
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
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
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

  val toDateBeforeFromDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06",
      |  "toDate": "2019-04-06",
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
    val nino: String       = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val submissionId       = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    def taxYear: String

    def setupStubs(): Unit = ()

    def downstreamUri: String

    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/uk/$nino/$businessId/period/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submissionId": "$submissionId",
         |  "links": [
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"PUT",
         |      "rel":"amend-uk-property-period-summary"
         |    },
         |    {
         |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/$submissionId",
         |      "method":"GET",
         |      "rel":"self"
         |    }
         |  ]
         |}
      """.stripMargin
    )

    val ifsResponse: JsValue = Json.parse(
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
    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/business/property/periodic/23-24"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId
    )

  }

  private trait NonTysTest extends Test {
    def taxYear: String       = "2022-23"
    def downstreamUri: String = s"/income-tax/business/property/periodic"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> "2022-23"
    )

  }

  "calling the create endpoint" should {

    "return a 201 status" when {

      "any valid unconsolidated request is made" in new NonTysTest {
        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, ifsResponse)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid consolidated request is made" in new NonTysTest {
        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, ifsResponse)
        }

        val response: WSResponse = await(request().post(requestBodyJsonConsolidatedExpense))
        response.status shouldBe CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for TYS" in new TysIfsTest {
        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, ifsResponse)
        }

        val response: WSResponse = await(request().post(requestBodyJsonConsolidatedExpense))
        response.status shouldBe CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return bad request error" when {
      "badly formed json body" in new NonTysTest {
        private val json =
          s"""
             |{
             | badJson
             | }
           """.stripMargin

        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).post(json))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(BadRequestError)
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
            override val taxYear: String    = requestTaxYear

            val response: WSResponse = await(request().post(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "XAIS12345678910", "2022-23", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "20223", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2021-23", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2021-22", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XA***IS1", "2022-23", requestBodyJson, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidToDateRequestJson, BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidFromDateRequestJson, BAD_REQUEST, FromDateFormatError),
          (
            "AA123456A",
            "XAIS12345678910",
            "2022-23",
            Json.parse(s"""{ "fromDate": "2020-04-06", "toDate": "2019-04-06", "ukFhlProperty": {} }""".stripMargin),
            BAD_REQUEST,
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/ukFhlProperty")))),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidValueRequestJson, BAD_REQUEST, allInvalidValueRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", bothExpensesSuppliedRequestJson, BAD_REQUEST, RuleBothExpensesSuppliedRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", toDateBeforeFromDateRequestJson, BAD_REQUEST, RuleToDateBeforeFromDateError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new NonTysTest {

            override def setupStubs(): Unit = {
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamQueryParams, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
          (CONFLICT, "DUPLICATE_SUBMISSION", BAD_REQUEST, RuleDuplicateSubmissionError),
          (UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", BAD_REQUEST, RuleOverlappingPeriodError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "GAPS_IN_PERIOD", BAD_REQUEST, RuleNotContiguousPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleToDateBeforeFromDateError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "PERIOD_NOT_ALIGNED", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "SUBMISSION_DATE_ISSUE", BAD_REQUEST, RuleMisalignedPeriodError),
          (UNPROCESSABLE_ENTITY, "BUSINESS_INCOME_PERIOD_RESTRICTION", INTERNAL_SERVER_ERROR, InternalError)
          //          (UNPROCESSABLE_ENTITY, "INVALID_SUBMISSION_PERIOD", BAD_REQUEST, RuleInvalidSubmissionPeriodError),
          //          (UNPROCESSABLE_ENTITY, "INVALID_SUBMISSION_END_DATE", BAD_REQUEST, RuleInvalidSubmissionEndDateError)
          //          To be reinstated, see MTDSA-15575
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
