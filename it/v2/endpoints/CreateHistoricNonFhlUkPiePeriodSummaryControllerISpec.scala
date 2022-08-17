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
import v2.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateHistoricNonFhlUkPiePeriodSummaryControllerISpec extends V2IntegrationBaseSpec {

  private val requestBodyJson = Json.parse(
    """{
      | "fromDate": "2019-03-11",
      | "toDate": "2020-04-23",
      |   "income": {
      |   "periodAmount": 123.45,
      |   "premiumsOfLeaseGrant": 2355.45,
      |   "reversePremiums": 454.56,
      |   "otherIncome": 567.89,
      |   "taxDeducted": 234.53,
      |   "rentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "expenses":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpense = Json.parse(
    """{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}""".stripMargin
  )

  val invalidToDateRequestJson: JsValue = Json.parse(
    """{
      |    "fromDate": "2019-03-11",
      |    "toDate": "20200423",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}""".stripMargin
  )

  val invalidFromDateRequestJson: JsValue = Json.parse(
    """{
      |    "fromDate": "20190311",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}""".stripMargin
  )

  val invalidValueRequestJson:  JsValue = Json.parse(
    """{
      | "fromDate": "2019-03-11",
      | "toDate": "2020-04-23",
      |   "income": {
      |   "periodAmount": 123.459999999999999999999999,
      |   "premiumsOfLeaseGrant": 2355.45,
      |   "reversePremiums": 454.56,
      |   "otherIncome": 567.89,
      |   "taxDeducted": 234.53,
      |   "rentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "expenses":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.99
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val bothExpensesSuppliedRequestJson: JsValue = Json.parse(
    """{
      | "fromDate": "2019-03-11",
      | "toDate": "2020-04-23",
      |   "income": {
      |   "periodAmount": 123.45,
      |   "premiumsOfLeaseGrant": 2355.45,
      |   "reversePremiums": 454.56,
      |   "otherIncome": 567.89,
      |   "taxDeducted": 234.53,
      |   "rentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "expenses":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    },
      |    "consolidatedExpenses": 235.78
      |  }
      |}""".stripMargin
  )

  val toDateBeforeFromDateRequestJson: JsValue = Json.parse(
    """{
      | "fromDate": "2020-03-11",
      | "toDate": "2019-04-23",
      |   "income": {
      |   "periodAmount": 123.45,
      |   "premiumsOfLeaseGrant": 2355.45,
      |   "reversePremiums": 454.56,
      |   "otherIncome": 567.89,
      |   "taxDeducted": 234.53,
      |   "rentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "expenses":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val missingFromAndToDateRequestJson: JsValue = Json.parse(
    """{
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}""".stripMargin
  )

  val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
    paths = Some(List(
      "/income/periodAmount"
    ))
  )

  trait Test {
    val nino: String = "TC663795B"

    def setupStubs(): StubMapping

    def uri: String = s"/uk/period/non-furnished-holiday-lettings/$nino"

    def ifsUri: String = s"/income-tax/nino/$nino/uk-properties/other/periodic-summaries"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    val responseBody: JsValue = Json.parse(
      s"""{
         |  "periodId": "2019-03-11_2020-04-23",
         |  "links": [
         |    {
         |      "href": "/individuals/business/property/uk/non-furnished-holiday-lettings/$nino/2019-03-11_2020-04-23",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/uk/non-furnished-holiday-lettings/$nino/2019-03-11_2020-04-23",
         |      "method": "PUT",
         |      "rel": "amend-uk-property-historic-non-fhl-period-summary"
         |    }
         |  ]
         |}""".stripMargin
    )

    val ifsResponse: JsValue = Json.parse(
      """{
        |  "transactionReference": "0000000000000001"
        |}""".stripMargin
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
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, Status.OK, ifsResponse)
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
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, Status.OK, ifsResponse)

        }

        val response: WSResponse = await(request().post(requestBodyJsonConsolidatedExpense))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return bad request error" when {
      "badly formed json body is submitted" in new Test {
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
        def validationErrorTest(requestNino: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino

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
          ("AA1123A", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", invalidToDateRequestJson, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", invalidFromDateRequestJson, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", bothExpensesSuppliedRequestJson, Status.BAD_REQUEST, RuleBothExpensesSuppliedError.copy(paths=Some(Seq("/expenses/consolidatedExpenses")))),
          ("AA123456A", missingFromAndToDateRequestJson, Status.BAD_REQUEST,  RuleIncorrectOrEmptyBodyError.copy(paths=Some(Seq("/fromDate", "/toDate")))),
          ("AA123456A", toDateBeforeFromDateRequestJson, Status.BAD_REQUEST, RuleToDateBeforeFromDateError),
          ("AA123456A", invalidValueRequestJson, Status.BAD_REQUEST, allInvalidValueRequestError)
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
              DownstreamStub.onError(DownstreamStub.POST, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_NINO", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TYPE", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.CONFLICT, "DUPLICATE_SUBMISSION", Status.BAD_REQUEST, RuleDuplicateSubmissionError),
          (Status.UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", Status.BAD_REQUEST, RuleMisalignedPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", Status.BAD_REQUEST, RuleOverlappingPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "NOT_CONTIGUOUS_PERIOD", Status.BAD_REQUEST, RuleNotContiguousPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_PERIOD", Status.BAD_REQUEST, RuleToDateBeforeFromDateError),
          (Status.UNPROCESSABLE_ENTITY, "BOTH_EXPENSES_SUPPLIED", Status.BAD_REQUEST, RuleBothExpensesSuppliedError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleHistoricTaxYearNotSupportedError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}