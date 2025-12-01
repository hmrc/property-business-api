/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyAnnualSubmission.def2

import common.models.errors.RuleTypeOfBusinessIncorrectError
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec

class Def2_RetrieveForeignPropertyAnnualSubmissionHipISpec extends IntegrationBaseSpec {

  "calling the retrieve foreign property annual submission endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUri,
          status = OK,
          body = downstreamResponseBody
        )

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {
            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input: Seq[(String, String, String, Int, MtdError)] = List(
          ("AA123", "XAIS12345678910", "2025-26", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2025-26", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2025", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a type $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): Unit = DownstreamStub.onError(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors: Seq[(Int, String, Int, MtdError)] = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors: Seq[(Int, String, Int, MtdError)]  = List(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => serviceErrorTest.tupled(args))
      }
    }

    "return 400 BAD_REQUEST RuleTypeOfBusinessIncorrectError" when {
      "the service response does not contain a foreign property" in new Test {
        override val downstreamResponseBody: JsValue = Json.parse(
          """
            |{
            |  "submittedOn": "2025-06-17T10:53:38.000Z",
            |  "ukProperty": {
            |     "allowances": {
            |        "annualInvestmentAllowance": 123.45
            |     }
            |  }
            |}
          """.stripMargin
        )

        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUri,
          status = OK,
          body = downstreamResponseBody
        )

        val response: WSResponse = await(request().get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(RuleTypeOfBusinessIncorrectError)
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678910"
    val taxYear: String    = "2025-26"

    val downstreamUri: String = s"/itsa/income-tax/v1/25-26/business/property/annual/$nino/$businessId"

    val responseBody: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2020-07-07T10:59:47.544Z",
        |  "foreignProperty": [
        |    {
        |      "countryCode": "DEU",
        |      "adjustments": {
        |        "privateUseAdjustment": 100.25,
        |        "balancingCharge": 100.25
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance": 100.25,
        |        "costOfReplacingDomesticItems": 100.25,
        |        "otherCapitalAllowance": 100.25,
        |        "zeroEmissionsCarAllowance": 100.25,
        |        "propertyIncomeAllowance": 100.25,
        |        "structuredBuildingAllowance": [
        |          {
        |            "amount": 100.25,
        |            "firstYear": {
        |              "qualifyingDate": "2020-03-29",
        |              "qualifyingAmountExpenditure": 100.25
        |            },
        |            "building": {
        |              "name": "Building Name",
        |              "number": "12",
        |              "postcode": "TF3 4GH"
        |            }
        |          }
        |        ]
        |      }
        |    }
        |  ]
        |}
     """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2020-07-07T10:59:47.544Z",
        |  "foreignProperty": [
        |    {
        |      "countryCode": "DEU",
        |      "adjustments": {
        |        "privateUseAdjustment": 100.25,
        |        "balancingCharge": 100.25
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance": 100.25,
        |        "costOfReplacingDomesticItems": 100.25,
        |        "otherCapitalAllowance": 100.25,
        |        "zeroEmissionsCarAllowance": 100.25,
        |        "propertyAllowance": 100.25,
        |        "structuredBuildingAllowance": [
        |          {
        |            "amount": 100.25,
        |            "firstYear": {
        |              "qualifyingDate": "2020-03-29",
        |              "qualifyingAmountExpenditure": 100.25
        |            },
        |            "building": {
        |              "name": "Building Name",
        |              "number": "12",
        |              "postCode": "TF3 4GH"
        |            }
        |          }
        |        ]
        |      }
        |    }
        |  ]
        |}
      """.stripMargin
    )

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/annual/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(`type`: String): String =
      s"""
        |{
        |  "origin": "HoD",
        |  "response": {
        |    "failures": [
        |      {
        |        "type": "${`type`}",
        |        "reason": "message"
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
  }

}
