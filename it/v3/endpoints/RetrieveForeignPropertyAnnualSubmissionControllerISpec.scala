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
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.V2IntegrationBaseSpec
import v3.stubs.{AuthStub, DownstreamStub, MtdIdLookupStub}

class RetrieveForeignPropertyAnnualSubmissionControllerISpec extends V2IntegrationBaseSpec {

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678910"
    def taxYear: String

    val responseBody: JsValue = Json.parse(
      s"""
        |{
        |  "submittedOn": "2020-07-07T10:59:47.544Z",
        |  "foreignFhlEea": {
        |    "adjustments": {
        |      "privateUseAdjustment": 100.25,
        |      "balancingCharge": 100.25,
        |      "periodOfGraceAdjustment": true
        |    },
        |    "allowances": {
        |      "annualInvestmentAllowance": 100.25,
        |      "otherCapitalAllowance": 100.25,
        |      "electricChargePointAllowance": 100.25,
        |      "zeroEmissionsCarAllowance": 100.25,
        |      "propertyIncomeAllowance": 100.25
        |    }
        |  },
        |  "foreignNonFhlProperty": [
        |    {
        |      "countryCode": "GER",
        |      "adjustments": {
        |        "privateUseAdjustment": 100.25,
        |        "balancingCharge": 100.25
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance": 100.25,
        |        "costOfReplacingDomesticItems": 100.25,
        |        "zeroEmissionsGoodsVehicleAllowance": 100.25,
        |        "otherCapitalAllowance": 100.25,
        |        "electricChargePointAllowance": 100.25,
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
        |  ],
        |  "links": [
        |    {
        |      "href": "/individuals/business/property/foreign/AA123456A/XAIS12345678910/annual/$taxYear",
        |      "method": "PUT",
        |      "rel": "create-and-amend-foreign-property-annual-submission"
        |    },
        |    {
        |      "href": "/individuals/business/property/foreign/AA123456A/XAIS12345678910/annual/$taxYear",
        |      "method": "GET",
        |      "rel": "self"
        |    },
        |    {
        |      "href": "/individuals/business/property/AA123456A/XAIS12345678910/annual/$taxYear",
        |      "method": "DELETE",
        |      "rel": "delete-property-annual-submission"
        |    }
        |  ]
        |}
     """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse("""
        |{
        |    "submittedOn": "2020-07-07T10:59:47.544Z",
        |    "deletedOn": "2021-11-04T08:23:42Z",
        |    "foreignFhlEea": {
        |      "adjustments": {
        |        "privateUseAdjustment": 100.25,
        |        "balancingCharge": 100.25,
        |        "periodOfGraceAdjustment": true
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance": 100.25,
        |        "otherCapitalAllowance": 100.25,
        |        "electricChargePointAllowance": 100.25,
        |        "zeroEmissionsCarAllowance": 100.25,
        |        "propertyAllowance": 100.25
        |      }
        |    },
        |    "foreignProperty": [
        |      {
        |        "countryCode": "GER",
        |        "adjustments": {
        |          "privateUseAdjustment": 100.25,
        |          "balancingCharge": 100.25
        |        },
        |        "allowances": {
        |          "annualInvestmentAllowance": 100.25,
        |          "costOfReplacingDomesticItems": 100.25,
        |          "zeroEmissionsGoodsVehicleAllowance": 100.25,
        |          "otherCapitalAllowance": 100.25,
        |          "electricChargePointAllowance": 100.25,
        |          "zeroEmissionsCarAllowance": 100.25,
        |          "propertyAllowance": 100.25,
        |          "structuredBuildingAllowance": [
        |            {
        |              "amount": 100.25,
        |              "firstYear": {
        |                "qualifyingDate": "2020-03-29",
        |                "qualifyingAmountExpenditure": 100.25
        |              },
        |              "building": {
        |                "name": "Building Name",
        |                "number": "12",
        |                "postCode": "TF3 4GH"
        |              }
        |            }
        |          ]
        |        }
        |      }
        |    ]
        |  }
        |""".stripMargin)

    def setupStubs(): Unit = ()

    def downstreamUri: String

    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/foreign/$nino/$businessId/annual/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "message"
         |}
       """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2021-22"

    def downstreamUri: String = s"/income-tax/business/property/annual"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> "2021-22"
    )

  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/business/property/annual/23-24/$nino/$businessId"

    def downstreamQueryParams: Map[String, String] = Map.empty
  }

  "calling the retrieve foreign property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test with NonTysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, downstreamResponseBody)

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for TYS" in new Test with TysIfsTest {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, downstreamResponseBody)

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
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
          s"validation fails with ${expectedBody.code} error" in new Test with NonTysTest {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA123", "XAIS12345678910", "2021-22", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2021-22", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2020", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2020-22", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", Status.BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test with NonTysTest {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.NOT_FOUND, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }

    "the service response does not contain a foreign property" in new Test with NonTysTest {
      override val downstreamResponseBody: JsValue = Json.parse(s"""
           |{
           |  "submittedOn": "2022-06-17T10:53:38.000Z",
           |  "ukFhlProperty": {
           |     "allowances": {
           |        "annualInvestmentAllowance": 123.45
           |     }
           |  }
           |}""".stripMargin)

      override def setupStubs(): Unit =
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, downstreamResponseBody)

      val response: WSResponse = await(request().get())
      response.status shouldBe Status.BAD_REQUEST
      response.json shouldBe Json.toJson(RuleTypeOfBusinessIncorrectError)
    }
  }

}
