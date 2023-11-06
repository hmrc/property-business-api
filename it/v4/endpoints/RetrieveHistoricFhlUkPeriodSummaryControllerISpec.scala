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

package v4.endpoints

import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, PeriodIdFormatError}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v4.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class RetrieveHistoricFhlUkPeriodSummaryControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino     = "TC663795B"
    val from     = "2017-04-06"
    val to       = "2017-07-04"
    val periodId = s"${from}_$to"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "fromDate": "2017-04-06",
         |  "toDate": "2017-07-04",
         |  "income":{
         |    "periodAmount":5000.99,
         |    "taxDeducted":5000.99,
         |    "rentARoom":{
         |      "rentsReceived":5000.99
         |    }
         |  },
         |  "expenses":{
         |    "premisesRunningCosts": 5000.99,
         |    "repairsAndMaintenance": 5000.99,
         |    "financialCosts": 5000.99,
         |    "professionalFees": 5000.99,
         |    "costOfServices": 5000.99,
         |    "other": 5000.99,
         |    "consolidatedExpenses": 5000.99,
         |    "travelCosts": 5000.99,
         |    "rentARoom":{
         |      "amountClaimed":5000.99
         |    }
         |  },
         |  "links": [
         |    {
         |      "href":"/individuals/business/property/uk/period/furnished-holiday-lettings/TC663795B/2017-04-06_2017-07-04",
         |      "method": "PUT",
         |      "rel": "amend-uk-property-historic-fhl-period-summary"
         |    },
         |    {
         |      "href":"/individuals/business/property/uk/period/furnished-holiday-lettings/TC663795B/2017-04-06_2017-07-04",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/TC663795B",
         |      "method": "GET",
         |      "rel": "list-uk-property-historic-fhl-period-summaries"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse("""
         |{
         |   "from": "2017-04-06",
         |   "to": "2017-07-04",
         |   "financials": {
         |      "incomes": {
         |         "rentIncome": {
         |            "amount": 5000.99,
         |            "taxDeducted": 5000.99
         |         },
         |         "premiumsOfLeaseGrant": 5000.99,
         |         "reversePremiums": 5000.99,
         |         "otherIncome": 5000.99,
         |        "ukRentARoom": {
         |            "rentsReceived": 5000.99
         |         }
         |      },
         |      "deductions": {
         |         "premisesRunningCosts": 5000.99,
         |         "repairsAndMaintenance": 5000.99,
         |         "financialCosts": 5000.99,
         |         "professionalFees": 5000.99,
         |         "costOfServices": 5000.99,
         |         "other": 5000.99,
         |         "consolidatedExpenses": 5000.99,
         |         "residentialFinancialCost": 5000.99,
         |         "travelCosts": 5000.99,
         |         "residentialFinancialCostsCarriedForward": 5000.99,
         |         "ukRentARoom": {
         |            "amountClaimed": 5000.99
         |         }
         |      }
         |   }
         |}
         |""".stripMargin)

    def setupStubs(): StubMapping

    def mtdUri: String        = s"/uk/period/furnished-holiday-lettings/$nino/$periodId"
    def downstreamUri: String = s"/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summary-detail"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "error message from downstream"
         |}
       """.stripMargin

  }

  "calling the retrieve uk historic FHL property income and expenses period summary endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("from" -> from, "to" -> to), OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestPeriodId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String     = requestNino
            override val periodId: String = requestPeriodId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA123", "2017-04-06_2017-07-04", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2017-07-04_2017-04-06", BAD_REQUEST, PeriodIdFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "Downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, PeriodIdFormatError),
          (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, PeriodIdFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND_PROPERTY", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NOT_FOUND_PERIOD", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
