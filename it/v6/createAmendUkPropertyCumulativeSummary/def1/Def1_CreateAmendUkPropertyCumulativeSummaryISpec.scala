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

package v6.createAmendUkPropertyCumulativeSummary.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec

class Def1_CreateAmendUkPropertyCumulativeSummaryISpec extends IntegrationBaseSpec with JsonErrorValidators {

  val validRequestBodyJson = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "rentARoom": {
      |        "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 1500.50,
      |      "repairsAndMaintenance": 1200.75,
      |      "financialCosts": 2000.00,
      |      "professionalFees": 500.00,
      |      "costOfServices": 300.25,
      |      "other": 100.50,
      |      "residentialFinancialCost": 9000.10,
      |      "travelCosts": 400.00,
      |      "residentialFinancialCostsCarriedForward": 300.13,
      |      "rentARoom": {
      |        "amountClaimed": 860.88
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  private trait Test {
    val nino: String          = "TC663795B"
    val businessId: String    = "XAIS12345678910"
    val correlationId: String = "X-123"

    def taxYear: String
    def downstreamTaxYear: String
    def downstreamUri: String

    val requestBodyJson: JsValue = validRequestBodyJson

    def setupStubs(): StubMapping

    private def uri: String = s"/uk/$nino/$businessId/cumulative/$taxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin

  }

  private trait TysIfsTest extends Test {
    def taxYear: String                            = "2025-26"
    def downstreamTaxYear: String                  = "25-26"
    def downstreamQueryParams: Map[String, String] = Map()

    override def downstreamUri: String = s"/income-tax/$downstreamTaxYear/business/property/periodic/$nino/$businessId"
  }

  "Calling the create amend uk property cumulative summary endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, Status.NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return a 400 with multiple errors" when {
      "all field validations fail on the request body" in new TysIfsTest {

        val allInvalidFieldsRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "fromDate": "2023-456780-01",
            |  "toDate": "2024-9876540-01",
            |  "ukProperty": {
            |    "income": {
            |      "premiumsOfLeaseGrant": 42.123456789,
            |      "reversePremiums": 84.98765432,
            |      "periodAmount": 9884.987654321,
            |      "taxDeducted": 842.987654321,
            |      "otherIncome": 31.987654321,
            |      "rentARoom": {
            |        "rentsReceived": 947.987654321
            |      }
            |    },
            |    "expenses": {
            |      "premisesRunningCosts": 1500.987654321,
            |      "repairsAndMaintenance": 1200.987654321,
            |      "financialCosts": 2000.987654321,
            |      "professionalFees": 500.987654321,
            |      "costOfServices": 300.987654321,
            |      "other": 100.987654321,
            |      "residentialFinancialCost": 9000.987654321,
            |      "travelCosts": 400.987654321,
            |      "residentialFinancialCostsCarriedForward": 300.987654321,
            |      "rentARoom": {
            |        "amountClaimed": 860.987654321
            |      }
            |    }
            |  }
            |}
          """.stripMargin
        )

        val allInvalidFieldsRequestError: List[MtdError] = List(
          FromDateFormatError,
          ToDateFormatError,
          ValueFormatError.copy(
            message = "The value must be between -99999999999.99 and 99999999999.99",
            paths = Some(
              List(
                "/ukProperty/expenses/premisesRunningCosts",
                "/ukProperty/expenses/repairsAndMaintenance",
                "/ukProperty/expenses/financialCosts",
                "/ukProperty/expenses/professionalFees",
                "/ukProperty/expenses/costOfServices",
                "/ukProperty/expenses/other",
                "/ukProperty/expenses/travelCosts"
              )
            )
          ),
          ValueFormatError.copy(
            message = "The value must be between 0 and 99999999999.99",
            paths = Some(
              List(
                "/ukProperty/income/premiumsOfLeaseGrant",
                "/ukProperty/income/reversePremiums",
                "/ukProperty/income/periodAmount",
                "/ukProperty/income/taxDeducted",
                "/ukProperty/income/otherIncome",
                "/ukProperty/income/rentARoom/rentsReceived",
                "/ukProperty/expenses/residentialFinancialCost",
                "/ukProperty/expenses/residentialFinancialCostsCarriedForward",
                "/ukProperty/expenses/rentARoom/amountClaimed"
              )
            )
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidFieldsRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return an error according to spec" when {

      val allInvalidStringRequestBodyJson = Json.parse(
        """
          |{
          |  "fromDate": "2023-04-01",
          |  "toDate": "2024-04-01",
          |  "ukProperty": {
          |    "income": {
          |      "premiumsOfLeaseGrant": 42.12,
          |      "reversePremiums": 84.31,
          |      "periodAmount": 9884.93,
          |      "taxDeducted": 842.99,
          |      "otherIncome": 31.44,
          |      "rentARoom": {
          |        "rentsReceived": "947.66*"
          |      }
          |    },
          |    "expenses": {
          |      "premisesRunningCosts": 1500.50,
          |      "repairsAndMaintenance": 1200.75,
          |      "financialCosts": 2000.00,
          |      "professionalFees": 500.00,
          |      "costOfServices": 300.25,
          |      "other": 100.50,
          |      "residentialFinancialCost": 9000.10,
          |      "travelCosts": 400.00,
          |      "residentialFinancialCostsCarriedForward": 300.13,
          |      "rentARoom": {
          |        "amountClaimed": "860.88*"
          |      }
          |    }
          |  }
          |}
        """.stripMargin
      )

      val allInvalidBody: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        message = "An empty or non-matching body was submitted",
        paths = Some(
          List(
            "/ukProperty/expenses/rentARoom/amountClaimed",
            "/ukProperty/income/rentARoom/rentsReceived"
          ))
      )

      "validation error occurs" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new TysIfsTest {

            override val nino: String             = requestNino
            override val businessId: String       = requestBusinessId
            override val taxYear: String          = requestTaxYear
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

        val input = List(
          ("AA1123A", "XAIS12345678910", "2025-26", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "XA***IS1", "2025-26", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError, None),
          ("AA123456A", "XAIS12345678910", "20256", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "XAIS12345678910", "2025-27", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "XAIS12345678910", "2024-25", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            validRequestBodyJson.update("/fromDate", JsString("XX")),
            BAD_REQUEST,
            FromDateFormatError,
            None
          ),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            validRequestBodyJson.update("/toDate", JsString("XX")),
            BAD_REQUEST,
            ToDateFormatError,
            None
          ),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            validRequestBodyJson.removeProperty("/fromDate"),
            BAD_REQUEST,
            RuleMissingSubmissionDatesError,
            Some("for a missing fromDate")
          ),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            validRequestBodyJson.removeProperty("/toDate"),
            BAD_REQUEST,
            RuleMissingSubmissionDatesError,
            Some("for a missing toDate")
          ),
          (
            "AA123456A",
            "XAIS12345678910",
            "2025-26",
            validRequestBodyJson.update("/toDate", JsString("1999-01-01")),
            BAD_REQUEST,
            RuleToDateBeforeFromDateError,
            None
          ),
          ("AA123456A", "XAIS12345678910", "2025-26", allInvalidStringRequestBodyJson, BAD_REQUEST, allInvalidBody, None)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new TysIfsTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOME_SOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INVALID_SUBMISSION_END_DATE", BAD_REQUEST, RuleAdvanceSubmissionRequiresPeriodEndDateError),
          (UNPROCESSABLE_ENTITY, "SUBMISSION_END_DATE_VALUE", BAD_REQUEST, RuleSubmissionEndDateCannotMoveBackwardsError),
          (UNPROCESSABLE_ENTITY, "INVALID_START_DATE", BAD_REQUEST, RuleStartDateNotAlignedWithReportingTypeError),
          (UNPROCESSABLE_ENTITY, "START_DATE_NOT_ALIGNED", BAD_REQUEST, RuleStartDateNotAlignedToCommencementDateError),
          (UNPROCESSABLE_ENTITY, "END_DATE_NOT_ALIGNED", BAD_REQUEST, RuleEndDateNotAlignedWithReportingTypeError),
          (UNPROCESSABLE_ENTITY, "MISSING_SUBMISSION_DATES", BAD_REQUEST, RuleMissingSubmissionDatesError),
          (UNPROCESSABLE_ENTITY, "START_END_DATE_NOT_ACCEPTED", BAD_REQUEST, RuleStartAndEndDateNotAllowedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "EARLY_DATA_SUBMISSION_NOT_ACCEPTED", BAD_REQUEST, RuleEarlyDataSubmissionNotAcceptedError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "SUBMITTED_TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        errors.foreach(args => (serviceErrorTest _).tupled(args))
      }

    }
  }

}
