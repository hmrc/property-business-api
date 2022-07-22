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
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.V2IntegrationBaseSpec
import v2.models.errors._
import v2.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateUkPropertyPeriodSummaryControllerISpec extends V2IntegrationBaseSpec {

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

  val invalidValueRequestJson:  JsValue = Json.parse(
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
    paths = Some(List(
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
    paths = Some(List(
      "/ukFhlProperty/expenses",
      "/ukNonFhlProperty/expenses"
    ))
  )

  private trait Test {
    val nino: String = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val taxYear = "2022-23"

    def setupStubs(): StubMapping

    def uri: String = s"/uk/$nino/$businessId/period/$taxYear"

    def ifsUri: String = s"/income-tax/business/property/periodic"

    def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId" -> businessId,
      "taxYear" -> taxYear
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
      )
    }

    val responseBody: JsValue = Json.parse(
      s"""
        |{
        |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |  "links": [
        |    {
        |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |      "method":"PUT",
        |      "rel":"amend-uk-property-period-summary"
        |    },
        |    {
        |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |      "method":"GET",
        |      "rel":"self"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val ifsResponse: JsValue = Json.parse(
      """
        |{
        |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
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

  "calling the create endpoint" should {

    "return a 201 status" when {

      "any valid unconsolidated request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, ifsQueryParams, Status.OK, ifsResponse)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid consolidated request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, ifsQueryParams, Status.OK, ifsResponse)

        }

        val response: WSResponse = await(request().post(requestBodyJsonConsolidatedExpense))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return bad request error" when {
      "badly formed json body" in new Test {
        private val json =
          s"""
             |{
             | badJson
             | }
           """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }
        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).post(json))
        response.status shouldBe Status.BAD_REQUEST
        response.json shouldBe Json.toJson(BadRequestError)
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestBusinessId: String,
                                requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "XAIS12345678910", "2022-23", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "20223", requestBodyJson, Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2021-23", requestBodyJson, Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2021-22", requestBodyJson, Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XA***IS1", "2022-23", requestBodyJson, Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidToDateRequestJson, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidFromDateRequestJson, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", Json.parse(s"""{ "fromDate": "2020-04-06",
                                                                    |  "toDate": "2019-04-06",
                                                                    |  "ukFhlProperty": {}
                                                                    |  }""".stripMargin), BAD_REQUEST,
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/ukFhlProperty")))),
          ("AA123456A", "XAIS12345678910", "2022-23", invalidValueRequestJson, Status.BAD_REQUEST, allInvalidValueRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", bothExpensesSuppliedRequestJson, Status.BAD_REQUEST, RuleBothExpensesSuppliedRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", toDateBeforeFromDateRequestJson, Status.BAD_REQUEST, RuleToDateBeforeFromDateError)
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
              DownstreamStub.onError(DownstreamStub.POST, ifsUri, ifsQueryParams, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.CONFLICT, "DUPLICATE_SUBMISSION", Status.BAD_REQUEST, RuleDuplicateSubmissionError),
          (Status.UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", Status.BAD_REQUEST, RuleOverlappingPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", Status.BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", Status.BAD_REQUEST, RuleMisalignedPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "GAPS_IN_PERIOD", Status.BAD_REQUEST, RuleNotContiguousPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", Status.BAD_REQUEST, RuleToDateBeforeFromDateError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}