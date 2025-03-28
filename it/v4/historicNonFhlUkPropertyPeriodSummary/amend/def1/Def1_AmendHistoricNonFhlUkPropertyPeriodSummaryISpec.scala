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

package v4.historicNonFhlUkPropertyPeriodSummary.amend.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.{PeriodIdFormatError, RuleBothExpensesSuppliedError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{Format, JsNumber, JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.services._
import shared.support.IntegrationBaseSpec
import v4.historicNonFhlUkPropertyPeriodSummary.amend.model.request.Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody

class Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryISpec extends IntegrationBaseSpec with JsonErrorValidators {

  private def downstreamBody[A: Format](value: JsValue) = Json.toJson(value.as[A])

  val requestBodyJson: JsValue = Json.parse(
    """{
      |   "income":{
      |      "periodAmount": 1123.45,
      |      "premiumsOfLeaseGrant": 5000.99,
      |      "reversePremiums": 5000.99,
      |      "otherIncome": 5000.99,
      |      "taxDeducted": 2134.53,
      |      "rentARoom":{
      |         "rentsReceived": 5167.56
      |       }
      |   },
      |   "expenses":{
      |      "premiseRunningCosts": 5167.53,
      |      "repairsAndMaintenance": 424.65,
      |      "financialCosts": 853.56,
      |      "professionalFees": 835.78,
      |      "costOfServices": 978.34,
      |      "other": 382.34,
      |      "travelCosts": 145.56,
      |      "residentialFinancialCostsCarriedForward": 5000.99,
      |      "residentialFinancialCost": 5000.99,
      |      "rentARoom":{
      |         "amountClaimed": 945.9
      |       }
      |   }
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "premiumsOfLeaseGrant": 5000.99,
      |      "reversePremiums": 5000.99,
      |      "otherIncome": 5000.99,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin
  )

  private trait Test {
    val nino: String     = "AA123456A"
    val periodId: String = "2017-04-06_2017-07-04"

    def setupStubs(): StubMapping

    def ifsUri: String = s"/income-tax/nino/$nino/uk-properties/other/periodic-summaries"

    def ifsQueryParams: Map[String, String] = Map(
      "from" -> "2017-04-06",
      "to"   -> "2017-07-04"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/uk/period/non-furnished-holiday-lettings/$nino/$periodId")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
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

    val successDownstreamBody: String =
      """{
        |   "transactionReference": "ignored"
        |}
        |""".stripMargin

  }

  "calling the amend uk property period summary endpoint" should {
    "return a 200 status code" when {
      "any valid unconsolidated request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(DownstreamStub.PUT, ifsUri, queryParams = ifsQueryParams)
            .withRequestBody(downstreamBody[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody](requestBodyJson))
            .thenReturn(OK, successDownstreamBody)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid consolidated request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(DownstreamStub.PUT, ifsUri, queryParams = ifsQueryParams)
            .withRequestBody(downstreamBody[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody](requestBodyJsonConsolidatedExpenses))
            .thenReturn(OK, successDownstreamBody)
        }

        val response: WSResponse = await(request().put(requestBodyJsonConsolidatedExpenses))
        response.status shouldBe OK
        response.body shouldBe ""
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestPeriodId: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String     = requestNino
          override val periodId: String = requestPeriodId

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        ("AA1123A", "2017-04-06_2017-07-04", requestBodyJson, BAD_REQUEST, NinoFormatError),
        (
          "AA123456A",
          "2017-04-06_2017-07-04",
          requestBodyJsonConsolidatedExpenses.update("/expenses/premisesRunningCosts", JsNumber(1)),
          BAD_REQUEST,
          RuleBothExpensesSuppliedError.copy(paths = Some(List("/expenses/consolidatedExpenses")))),
        (
          "AA123456A",
          "2017-04-06_2017-07-04",
          requestBodyJson.update("/expenses/premisesRunningCosts", JsNumber(-1)),
          BAD_REQUEST,
          ValueFormatError.forPathAndRange("/expenses/premisesRunningCosts", min = "0", max = "99999999999.99")),
        ("AA123456A", "2017-04-06_2017-07-04", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
        ("AA123456A", "BAD_PERIOD_ID", requestBodyJson, BAD_REQUEST, PeriodIdFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return ifs service error" when {
      def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.PUT, ifsUri, ifsQueryParams, ifsStatus, errorBody(ifsCode))
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, PeriodIdFormatError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, PeriodIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "SUBMISSION_PERIOD_NOT_FOUND", NOT_FOUND, NotFoundError),
        (NOT_FOUND, "NOT_FOUND_PROPERTY", NOT_FOUND, NotFoundError),
        (NOT_FOUND, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "BOTH_EXPENSES_SUPPLIED", BAD_REQUEST, RuleBothExpensesSuppliedError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
