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

import api.models.errors._
import api.models.utils.JsonErrorValidators
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateHistoricFhlUkPeriodSummaryControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  val validRequestJson: JsValue = Json.parse(
    """
      | {
      |   "fromDate": "2017-04-06",
      |   "toDate": "2017-07-05",
      |   "income": {
      |     "periodAmount": 100.25,
      |     "taxDeducted": 100.25,
      |     "rentARoom": {
      |       "rentsReceived": 100.25
      |     }
      |   },
      |   "expenses": {
      |     "premisesRunningCosts": 100.25,
      |     "repairsAndMaintenance": 100.25,
      |     "financialCosts": 100.25,
      |     "professionalFees": 100.25,
      |     "costOfServices": 100.25,
      |     "travelCosts": 100.25,
      |     "other": 100.25,
      |     "rentARoom": {
      |       "amountClaimed": 100.25
      |     }
      |   }
      | }
      """.stripMargin
  )

  val invalidFieldsRequestBodyJson: JsValue = Json.parse(
    """
      | {
      |   "fromDate": "2017-04-06",
      |   "toDate": "2017-07-05",
      |   "income": {
      |     "periodAmount": -200.11,
      |     "taxDeducted": 100.25,
      |     "rentARoom": {
      |       "rentsReceived": -1
      |     }
      |   },
      |   "expenses": {
      |     "premisesRunningCosts": -11.99,
      |     "repairsAndMaintenance": 100.25,
      |     "financialCosts": 100.25,
      |     "professionalFees": 100.25,
      |     "costOfServices": 100.25,
      |     "travelCosts": 100.25,
      |     "other": 100.25,
      |     "rentARoom": {
      |       "amountClaimed": -1.23
      |     }
      |   }
      | }
      """.stripMargin
  )

  val valueFormatErrorForInvalidFieldsJson: MtdError = ValueFormatError.copy(
    paths = Some(
      List(
        "/income/periodAmount",
        "/income/rentARoom/rentsReceived",
        "/expenses/premisesRunningCosts",
        "/expenses/rentARoom/amountClaimed"
      ))
  )

  val invalidFromDateRequestBodyJson: JsValue = Json.parse(
    """
      | {
      |   "fromDate": "2017-04-0611111",
      |   "toDate": "2017-07-05",
      |   "expenses":{
      |       "consolidatedExpenses": 1
      |   }
      | }
      """.stripMargin
  )

  val invalidToDateRequestBodyJson: JsValue = Json.parse(
    """
      | {
      |   "fromDate": "2017-04-06",
      |   "toDate": "2017-07-0522222",
      |   "expenses":{
      |       "consolidatedExpenses": 1
      |   }
      | }
      """.stripMargin
  )

  private trait Test {

    val nino: String             = "TC663795B"
    val correlationId: String    = "X-123"
    val requestBodyJson: JsValue = validRequestJson

    def setupStubs(): StubMapping

    def mtdUri: String = s"/uk/period/furnished-holiday-lettings/$nino"

    def downstreamUri: String = s"/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries"

    val downstreamResponse: JsValue = Json.parse("""
        |{
        |   "transactionReference": "0000000000000001"
        |}
        |""".stripMargin)

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "Error message from downstream"
         |}
       """.stripMargin

  }

  "Calling the Create Historic FHL UK Property Income & Expenses Period Summary endpoint" should {
    "return a 201 status" when {
      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, OK, downstreamResponse)
        }

        val expectedResponseBody: JsValue = Json.parse(
          s"""
             |{
             |    "periodId": "2017-04-06_2017-07-05",
             |    "links": [
             |        {
             |            "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/TC663795B/2017-04-06_2017-07-05",
             |            "method": "PUT",
             |            "rel": "amend-uk-property-historic-fhl-period-summary"
             |        },
             |        {
             |            "href": "/individuals/business/property/uk/period/furnished-holiday-lettings/TC663795B/2017-04-06_2017-07-05",
             |            "method": "GET",
             |            "rel": "self"
             |        }
             |    ]
             |}
      """.stripMargin
        )

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe CREATED
        response.json shouldBe expectedResponseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return a 400 with multiple errors" when {
      "multiple field validations fail on the request body" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
        }

        val expectedWrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = valueFormatErrorForInvalidFieldsJson
        )

        val response: WSResponse = await(request().post(invalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(expectedWrappedErrors)
      }
    }

    "return a validation error according to spec" when {
      def validationErrorTest(requestNino: String, requestBody: JsValue, expectedStatus: Int, expectedError: MtdError): Unit = {
        s"validation fails with ${expectedError.code} error" in new Test {

          override val nino: String             = requestNino
          override val requestBodyJson: JsValue = requestBody

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().post(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedError)
        }
      }
      val input = Seq(
        ("AA1123A", validRequestJson, BAD_REQUEST, NinoFormatError),
        ("AA123456A", invalidFieldsRequestBodyJson, BAD_REQUEST, valueFormatErrorForInvalidFieldsJson),
        ("AA123456A", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
        ("AA123456A", invalidFromDateRequestBodyJson, BAD_REQUEST, FromDateFormatError),
        ("AA123456A", invalidToDateRequestBodyJson, BAD_REQUEST, ToDateFormatError),
        ("AA123456A", validRequestJson.update("/fromDate", JsString("2099-01-01")), BAD_REQUEST, RuleToDateBeforeFromDateError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "map each downstream service error to an MTD error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream responds with $downstreamCode and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request().post(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        (NO_CONTENT, "NO_CONTENT", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
        (CONFLICT, "DUPLICATE_SUBMISSION", BAD_REQUEST, RuleDuplicateSubmissionError),
        (UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", BAD_REQUEST, RuleMisalignedPeriodError),
        (UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", BAD_REQUEST, RuleOverlappingPeriodError),
        (UNPROCESSABLE_ENTITY, "NOT_CONTIGUOUS_PERIOD", BAD_REQUEST, RuleNotContiguousPeriodError),
        (UNPROCESSABLE_ENTITY, "INVALID_PERIOD", BAD_REQUEST, RuleToDateBeforeFromDateError),
        (UNPROCESSABLE_ENTITY, "BOTH_EXPENSES_SUPPLIED", BAD_REQUEST, RuleBothExpensesSuppliedError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleHistoricTaxYearNotSupportedError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
