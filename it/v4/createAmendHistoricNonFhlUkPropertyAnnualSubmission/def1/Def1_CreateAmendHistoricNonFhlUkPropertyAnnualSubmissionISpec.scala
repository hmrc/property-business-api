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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec

class Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String          = "AA999999A"
    val taxYear: String       = "2020-21"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |   "annualAdjustments": {
        |      "lossBroughtForward": 200.00,
        |      "balancingCharge": 200.00,
        |      "privateUseAdjustment": 200.00,
        |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
        |      "nonResidentLandlord": true,
        |      "rentARoom": {
        |         "jointlyLet": true
        |      }
        |   },
        |   "annualAllowances": {
        |      "annualInvestmentAllowance": 200.00,
        |      "zeroEmissionGoodsVehicleAllowance": 200.00,
        |      "businessPremisesRenovationAllowance": 200.00,
        |      "otherCapitalAllowance": 200.00,
        |      "costOfReplacingDomesticGoods": 200.00,
        |      "propertyIncomeAllowance": 30.02
        |   }
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String = s"/uk/annual/non-furnished-holiday-lettings/$nino/$taxYear"

    def downstreamUri: String = s"/income-tax/nino/$nino/uk-properties/other/annual-summaries/2021"

    val downstreamResponse: JsValue = Json.parse("""
        |{
        |   "transactionReference": "0000000000000001"
        |}
        |""".stripMargin)

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "downstream message"
         |}
       """.stripMargin

  }

  "Calling the create and amend historic FHL uk property annual submission endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, OK, downstreamResponse)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
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

        val invalidFieldsRequestBodyJson: JsValue = Json.parse("""
            |{
            |   "annualAdjustments": {
            |      "lossBroughtForward": -200.00,
            |      "balancingCharge": -100.00,
            |      "privateUseAdjustment": 20000000000000000000000.00,
            |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
            |      "nonResidentLandlord": true,
            |      "rentARoom": {
            |         "jointlyLet": true
            |      }
            |   },
            |   "annualAllowances": {
            |      "annualInvestmentAllowance": -200.00,
            |      "zeroEmissionGoodsVehicleAllowance": 200.00,
            |      "businessPremisesRenovationAllowance": 200.00,
            |      "otherCapitalAllowance": 200.00,
            |      "costOfReplacingDomesticGoods": 200.00,
            |      "propertyIncomeAllowance": 30.02
            |   }
            |}
            |""".stripMargin)

        val invalidFieldsRequestError: MtdError = ValueFormatError.copy(
          paths = Some(
            List(
              "/annualAdjustments/lossBroughtForward",
              "/annualAdjustments/privateUseAdjustment",
              "/annualAdjustments/balancingCharge",
              "/annualAllowances/annualInvestmentAllowance"
            ))
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = invalidFieldsRequestError
        )

        val response: WSResponse = await(request().put(invalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return an validation error according to spec" when {

      val requestJson: JsValue = Json.parse(
        """
          |{
          |   "annualAdjustments": {
          |      "lossBroughtForward": 200.00,
          |      "balancingCharge": 200.00,
          |      "privateUseAdjustment": 200.00,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |         "jointlyLet": true
          |      }
          |   },
          |   "annualAllowances": {
          |      "annualInvestmentAllowance": 200.00,
          |      "zeroEmissionGoodsVehicleAllowance": 200.00,
          |      "businessPremisesRenovationAllowance": 200.00,
          |      "otherCapitalAllowance": 200.00,
          |      "costOfReplacingDomesticGoods": 200.00,
          |      "propertyIncomeAllowance": 30.02
          |   }
          |}
      """.stripMargin
      )

      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String             = requestNino
          override val taxYear: String          = requestTaxYear
          override val requestBodyJson: JsValue = requestBody

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        ("AA1123A", "2021-22", requestJson, BAD_REQUEST, NinoFormatError),
        ("AA123456A", "202362-23", requestJson, BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "2021-24", requestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "2015-16", requestJson, BAD_REQUEST, RuleHistoricTaxYearNotSupportedError),
        ("AA123456A", "2021-22", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        (NO_CONTENT, "NO_CONTENT", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (NOT_FOUND, "NOT_FOUND_PROPERTY", NOT_FOUND, NotFoundError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (GONE, "GONE", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleHistoricTaxYearNotSupportedError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
