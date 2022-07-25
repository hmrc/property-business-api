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
import v2.stubs.{AuthStub, IfsStub, MtdIdLookupStub}

class RetrieveForeignPropertyAnnualSubmissionControllerISpec extends V2IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val businessId: String = "XAIS12345678910"
    val taxYear: String = "2021-22"

    val responseBody: JsValue = Json.parse(
      """
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
        |      "href": "/individuals/business/property/foreign/AA123456A/XAIS12345678910/annual/2021-22",
        |      "method": "PUT",
        |      "rel": "create-and-amend-foreign-property-annual-submission"
        |    },
        |    {
        |      "href": "/individuals/business/property/foreign/AA123456A/XAIS12345678910/annual/2021-22",
        |      "method": "GET",
        |      "rel": "self"
        |    },
        |    {
        |      "href": "/individuals/business/property/AA123456A/XAIS12345678910/annual/2021-22",
        |      "method": "DELETE",
        |      "rel": "delete-property-annual-submission"
        |    }
        |  ]
        |}
     """.stripMargin
    )

    val ifsResponseBody: JsValue = Json.parse(
      """
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

    def setupStubs(): StubMapping

    def uri: String = s"/foreign/$nino/$businessId/annual/$taxYear"

    def ifsUri: String = s"/income-tax/business/property/annual"

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

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin
  }

  "calling the retrieve foreign property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.GET, ifsUri, ifsQueryParams, Status.OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestBusinessId: String, requestTaxYear: String,
                                expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear


            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

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

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {


            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              IfsStub.onError(IfsStub.GET, ifsUri, ifsQueryParams, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.NOT_FOUND, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}