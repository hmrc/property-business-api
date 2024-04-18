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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino              = "AA123456A"
    val mtdTaxYear        = "2020-21"
    val downstreamTaxYear = "2021"

    val responseBody: JsValue = Json.parse(
      s"""
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
         |      "otherCapitalAllowance": 200.00,
         |      "zeroEmissionGoodsVehicleAllowance": 200.00,
         |      "businessPremisesRenovationAllowance": 200.00,
         |      "costOfReplacingDomesticGoods": 200.00,
         |      "propertyIncomeAllowance": 30.02
         |   },
         |   "links": [
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "PUT",
         |         "rel": "create-and-amend-uk-property-historic-non-fhl-annual-submission"
         |      },
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "GET",
         |         "rel": "self"
         |      },
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "DELETE",
         |         "rel": "delete-uk-property-historic-non-fhl-annual-submission"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    val emptyResponseWithHateoasBody: JsValue = Json.parse(
      s"""
         |{
         |   "links": [
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "PUT",
         |         "rel": "create-and-amend-uk-property-historic-non-fhl-annual-submission"
         |      },
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "GET",
         |         "rel": "self"
         |      },
         |      {
         |         "href": "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/AA123456A/2020-21",
         |         "method": "DELETE",
         |         "rel": "delete-uk-property-historic-non-fhl-annual-submission"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse(
      """
        |{
        |   "annualAdjustments": {
        |      "lossBroughtForward": 200.00,
        |      "balancingCharge": 200.00,
        |      "privateUseAdjustment": 200.00,
        |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
        |      "nonResidentLandlord": true,
        |      "ukRentARoom": {
        |         "jointlyLet": true
        |      }
        |   },
        |   "annualAllowances": {
        |      "annualInvestmentAllowance": 200.00,
        |      "otherCapitalAllowance": 200.00,
        |      "zeroEmissionGoodsVehicleAllowance": 200.00,
        |      "businessPremisesRenovationAllowance": 200.00,
        |      "costOfReplacingDomGoods": 200.00,
        |      "propertyIncomeAllowance": 30.02
        |   }
        |}
       """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String = s"/uk/annual/non-furnished-holiday-lettings/$nino/$mtdTaxYear"

    def downstreamUri: String = s"/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
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

  "calling the retrieve Non Fhl uk property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made but received empty json from downstream" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, Json.obj())
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe emptyResponseWithHateoasBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String       = requestNino
            override val mtdTaxYear: String = requestTaxYear

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

        val input = List(
          ("AA123", "2022-23", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2020", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2020-23", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2015-16", BAD_REQUEST, RuleHistoricTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "Downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $downstreamCode error and status $downstreamStatus" in new Test {

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

        val input = List(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NOT_FOUND_PERIOD", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleHistoricTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
