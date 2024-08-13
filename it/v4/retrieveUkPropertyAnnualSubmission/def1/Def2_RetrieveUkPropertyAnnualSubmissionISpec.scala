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

package v4.retrieveUkPropertyAnnualSubmission.def1

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v4.stubs._

class Def2_RetrieveUkPropertyAnnualSubmissionISpec extends IntegrationBaseSpec {

  "calling the retrieve uk property annual submission endpoint" should {

    "return a 200 status code" when {
      "any valid request is made for TYS" in new TysIfsTest {
        override def setupStubs(): StubMapping =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponseBodyTys)

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

          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

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
          ("AA123", "XAIS12345678910", "2022-23", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "2020", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "203100", "2022-23", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2020-23", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))

      }

      "downstream returns no UK properties" in new NonTysTest {
        val downstreamResponseBody: JsValue = Json.parse("""
            |{
            |  "submittedOn":"2022-06-17T10:53:38.000Z",
            |  "foreignProperty": [
            |    {
            |      "countryCode": "FRA",
            |      "adjustments": {
            |        "privateUseAdjustment": 100.25,
            |        "balancingCharge": 100.25
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance": 100.25,
            |        "costOfReplacingDomesticItems": 100.25,
            |        "zeroEmissionsGoodsVehicleAllowance": 100.25,
            |        "propertyAllowance": 100.25,
            |        "otherCapitalAllowance": 100.25,
            |        "structureAndBuildingAllowance": 100.25,
            |        "electricChargePointAllowance": 100.25
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponseBody)

        val response: WSResponse = await(request().get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(RuleTypeOfBusinessIncorrectError)
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678910"
    def taxYear: String

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submittedOn": "2022-06-17T10:53:38.000Z",
         |  "ukProperty": {
         |    "allowances": {
         |      "annualInvestmentAllowance": 678.45,
         |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
         |      "businessPremisesRenovationAllowance": 573.45,
         |      "otherCapitalAllowance": 452.34,
         |      "costOfReplacingDomesticGoods": 567.34,
         |      "propertyIncomeAllowance": 342.34,
         |      "electricChargePointAllowance": 454.34,
         |      "structuredBuildingAllowance": [
         |        {
         |          "amount": 234.34,
         |          "firstYear": {
         |            "qualifyingDate": "2020-03-29",
         |            "qualifyingAmountExpenditure": 3434.45
         |          },
         |          "building": {
         |            "name": "Plaza",
         |            "number": "1",
         |            "postcode": "TF3 4EH"
         |          }
         |        }
         |      ],
         |      "enhancedStructuredBuildingAllowance": [
         |        {
         |          "amount": 234.45,
         |          "firstYear": {
         |            "qualifyingDate": "2020-05-29",
         |            "qualifyingAmountExpenditure": 453.34
         |          },
         |          "building": {
         |            "name": "Plaza 2",
         |            "number": "2",
         |            "postcode": "TF3 4ER"
         |          }
         |        }
         |      ],
         |      "zeroEmissionsCarAllowance": 454.34
         |    },
         |    "adjustments": {
         |      "balancingCharge": 565.34,
         |      "privateUseAdjustment": 533.54,
         |      "businessPremisesRenovationAllowanceBalancingCharges": 563.34,
         |      "nonResidentLandlord": true,
         |      "rentARoom": {
         |        "jointlyLet": true
         |      }
         |    }
         |  }
         |}
       """.stripMargin
    )

    val downstreamResponseBodyTys: JsValue = Json.parse(
      """
        |{
        |   "submittedOn":"2022-06-17T10:53:38.000Z",
        |   "ukOtherProperty":{
        |      "ukOtherPropertyAnnualAllowances":{
        |         "annualInvestmentAllowance":678.45,
        |         "zeroEmissionGoodsVehicleAllowance":456.34,
        |         "businessPremisesRenovationAllowance":573.45,
        |         "otherCapitalAllowance":452.34,
        |         "costOfReplacingDomesticItems":567.34,
        |         "propertyIncomeAllowance":342.34,
        |         "electricChargePointAllowance":454.34,
        |         "structuredBuildingAllowance":[
        |            {
        |               "amount":234.34,
        |               "firstYear":{
        |                  "qualifyingDate":"2020-03-29",
        |                  "qualifyingAmountExpenditure":3434.45
        |               },
        |               "building":{
        |                  "name":"Plaza",
        |                  "number":"1",
        |                  "postCode":"TF3 4EH"
        |               }
        |            }
        |         ],
        |         "enhancedStructuredBuildingAllowance":[
        |            {
        |               "amount":234.45,
        |               "firstYear":{
        |                  "qualifyingDate":"2020-05-29",
        |                  "qualifyingAmountExpenditure":453.34
        |               },
        |               "building":{
        |                  "name":"Plaza 2",
        |                  "number":"2",
        |                  "postCode":"TF3 4ER"
        |               }
        |            }
        |         ],
        |         "zeroEmissionsCarAllowance":454.34
        |      },
        |      "ukOtherPropertyAnnualAdjustments":{
        |         "balancingCharge":565.34,
        |         "privateUseAdjustment":533.54,
        |         "businessPremisesRenovationAllowanceBalancingCharges":563.34,
        |         "nonResidentLandlord":true,
        |         "ukOtherRentARoom":{
        |            "jointlyLet":true
        |         }
        |      }
        |   }
        |}
       """.stripMargin
    )

    def setupStubs(): StubMapping
    def downstreamUri: String
    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/uk/$nino/$businessId/annual/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
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
    def taxYear: String       = "2022-23"
    def downstreamUri: String = s"/income-tax/business/property/annual"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> taxYear
    )

  }

  private trait TysIfsTest extends Test {
    def taxYear: String                            = "2024-25"
    def downstreamUri: String                      = s"/income-tax/business/property/annual/24-25/$nino/$businessId"
    def downstreamQueryParams: Map[String, String] = Map.empty
  }

}
