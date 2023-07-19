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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.V2IntegrationBaseSpec
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionFixture
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAmendForeignPropertyAnnualSubmissionControllerISpec extends V2IntegrationBaseSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  val requestBodyJson: JsValue = Json.parse("""
      |{
      |   "foreignFhlEea":{
      |      "adjustments":{
      |         "privateUseAdjustment":1.25,
      |         "balancingCharge":2.25,
      |         "periodOfGraceAdjustment":true
      |      },
      |      "allowances":{
      |         "annualInvestmentAllowance":1.25,
      |         "otherCapitalAllowance":2.25,
      |         "electricChargePointAllowance":3.25,
      |         "zeroEmissionsCarAllowance":4.25
      |      }
      |   },
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"IND",
      |         "adjustments":{
      |            "privateUseAdjustment":1.25,
      |            "balancingCharge":2.25
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":1.25,
      |            "costOfReplacingDomesticItems":2.25,
      |            "zeroEmissionsGoodsVehicleAllowance":3.25,
      |            "otherCapitalAllowance":4.25,
      |            "electricChargePointAllowance":5.25,
      |            "zeroEmissionsCarAllowance":6.25,
      |            "structuredBuildingAllowance":[
      |               {
      |                  "amount":3000.3,
      |                  "firstYear":{
      |                     "qualifyingDate":"2020-01-01",
      |                     "qualifyingAmountExpenditure":3000.4
      |                  },
      |                  "building":{
      |                     "name":"house name",
      |                     "number":"house number",
      |                     "postcode":"GF49JH"
      |                  }
      |               }
      |            ]
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val invalidFieldsTypeRequestBodyJson: JsValue = Json.parse("""
      |{
      |   "foreignFhlEea":{
      |      "adjustments":{
      |         "privateUseAdjustment":"OK",
      |         "balancingCharge":"OK",
      |         "periodOfGraceAdjustment":true
      |      },
      |      "allowances":{
      |         "annualInvestmentAllowance":"OK",
      |         "otherCapitalAllowance":"OK",
      |         "electricChargePointAllowance":3.25,
      |         "zeroEmissionsCarAllowance":4.25
      |      }
      |   },
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"IND",
      |         "adjustments":{
      |            "privateUseAdjustment":"OK",
      |            "balancingCharge":"OK"
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":"OK",
      |            "costOfReplacingDomesticItems":2.25,
      |            "zeroEmissionsGoodsVehicleAllowance":3.25,
      |            "otherCapitalAllowance":4.25,
      |            "electricChargePointAllowance":5.25,
      |            "zeroEmissionsCarAllowance":6.25,
      |            "structuredBuildingAllowance":[
      |               {
      |                  "amount":3000.3,
      |                  "firstYear":{
      |                     "qualifyingDate":1.25,
      |                     "qualifyingAmountExpenditure":3000.4
      |                  },
      |                  "building":{
      |                     "name":11,
      |                     "number":44,
      |                     "postcode":99
      |                  }
      |               }
      |            ]
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val invalidFieldsRequestBodyJson: JsValue = Json.parse("""
      |{
      |   "foreignFhlEea":{
      |      "adjustments":{
      |         "privateUseAdjustment":123.456,
      |         "balancingCharge":123.456,
      |         "periodOfGraceAdjustment":true
      |      },
      |      "allowances":{
      |         "annualInvestmentAllowance":123.456,
      |         "otherCapitalAllowance":123.456,
      |         "electricChargePointAllowance":3.25,
      |         "zeroEmissionsCarAllowance":4.25
      |      }
      |   },
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"IND",
      |         "adjustments":{
      |            "privateUseAdjustment":123.456,
      |            "balancingCharge":123.456
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":123.456,
      |            "costOfReplacingDomesticItems":2.25,
      |            "zeroEmissionsGoodsVehicleAllowance":3.25,
      |            "otherCapitalAllowance":4.25,
      |            "electricChargePointAllowance":5.25,
      |            "zeroEmissionsCarAllowance":6.25,
      |            "structuredBuildingAllowance":[
      |               {
      |                  "amount":3000.3,
      |                  "firstYear":{
      |                     "qualifyingDate":"9999-99-99",
      |                     "qualifyingAmountExpenditure":3000.4
      |                  },
      |                  "building":{
      |                     "name": "x * 91",
      |                     "number":"x * 91",
      |                     "postcode":"x * 91"
      |                  }
      |               }
      |            ]
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val ruleCountryCodeErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"QQQ",
      |         "adjustments":{
      |            "privateUseAdjustment":1.25,
      |            "balancingCharge":2.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val formatCountryCodeErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"QQQQ",
      |         "adjustments":{
      |            "privateUseAdjustment":1.25,
      |            "balancingCharge":2.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val duplicateCountryCodeErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignNonFhlProperty":[
      |      {
      |         "countryCode":"IND",
      |         "adjustments":{
      |            "privateUseAdjustment":1.25,
      |            "balancingCharge":2.25
      |         }
      |      },
      |      {
      |         "countryCode":"IND",
      |         "adjustments":{
      |            "privateUseAdjustment":1.25,
      |            "balancingCharge":2.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val rulePropertyIncomeAllowanceErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignFhlEea":{
      |      "adjustments":{
      |         "privateUseAdjustment":4.25,
      |         "balancingCharge":5.25,
      |         "periodOfGraceAdjustment":true
      |      },
      |      "allowances":{
      |         "propertyIncomeAllowance":5.25
      |      }
      |   }
      |}
      |""".stripMargin)

  val bothAllowancesSuppliedErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignFhlEea":{
      |      "allowances":{
      |         "annualInvestmentAllowance":3.25,
      |         "otherCapitalAllowance":4.25,
      |         "electricChargePointAllowance":3.25,
      |         "zeroEmissionsCarAllowance":4.25,
      |         "propertyIncomeAllowance":5.25
      |      }
      |   }
      |}
      |""".stripMargin)

  val ruleBuildingNameOrNumberErrorRequestJson: JsValue = Json.parse("""
      |{
      |   "foreignNonFhlProperty":[
      |      {
      |        "countryCode":"IND",
      |        "allowances":{
      |            "structuredBuildingAllowance":[
      |               {
      |                  "amount":3000.3,
      |                  "building":{
      |                    "postcode":"GF49JH"
      |                  }
      |               }
      |            ]
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  "Calling the amend foreign property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return a 400 with multiple errors" when {
      "field type validations fail on the request body" in new NonTysTest {

        val errorResponseJson: JsValue =
          Json.parse("""
            |{
            |   "code":"RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
            |   "message":"An empty or non-matching body was submitted",
            |   "paths":[
            |      "/foreignFhlEea/adjustments/balancingCharge",
            |      "/foreignFhlEea/adjustments/privateUseAdjustment",
            |      "/foreignFhlEea/allowances/annualInvestmentAllowance",
            |      "/foreignFhlEea/allowances/otherCapitalAllowance",
            |      "/foreignNonFhlProperty/0/adjustments/balancingCharge",
            |      "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment",
            |      "/foreignNonFhlProperty/0/allowances/annualInvestmentAllowance",
            |      "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/name",
            |      "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/number",
            |      "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/postcode",
            |      "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
            |   ]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(invalidFieldsTypeRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe errorResponseJson
      }

      "field data validations fail on the request body" in new NonTysTest {

        val allInvalidFieldsRequestErrors: List[MtdError] = List(
          DateFormatError.copy(
            paths = Some(
              List(
                "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
              ))
          ),
          StringFormatError.copy(
            paths = Some(
              List(
                "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/postcode",
                "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/name",
                "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/number"
              ))
          ),
          ValueFormatError.copy(
            paths = Some(
              List(
                "/foreignFhlEea/adjustments/privateUseAdjustment",
                "/foreignFhlEea/adjustments/balancingCharge",
                "/foreignFhlEea/allowances/annualInvestmentAllowance",
                "/foreignFhlEea/allowances/otherCapitalAllowance",
                "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment",
                "/foreignNonFhlProperty/0/adjustments/balancingCharge",
                "/foreignNonFhlProperty/0/allowances/annualInvestmentAllowance"
              ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidFieldsRequestErrors)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(invalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "validation error occurs" when {
      def validationErrorTest(requestNino: String,
                              requestBusinessId: String,
                              requestTaxYear: String,
                              requestBody: JsValue,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new NonTysTest {

          override val nino: String       = requestNino
          override val businessId: String = requestBusinessId
          override val taxYear: String    = requestTaxYear

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = Seq(
        ("AA1123A", "XAIS12345678910", "2021-22", requestBodyJson, BAD_REQUEST, NinoFormatError),
        ("AA123456A", "XAIS12345678910", "202362-23", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "XAIS1234dfxgchjbn5678910", "2021-22", requestBodyJson, BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "XAIS12345678910", "2021-24", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "XAIS12345678910", "2018-19", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
        (
          "AA123456A",
          "XAIS12345678910",
          "2021-22",
          Json.parse(s"""{
             |
             |}""".stripMargin),
          BAD_REQUEST,
          RuleIncorrectOrEmptyBodyError),
        (
          "AA123456A",
          "XAIS12345678910",
          "2021-22",
          ruleCountryCodeErrorRequestJson,
          BAD_REQUEST,
          RuleCountryCodeError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode")))),
        (
          "AA123456A",
          "XAIS12345678910",
          "2021-22",
          formatCountryCodeErrorRequestJson,
          BAD_REQUEST,
          CountryCodeFormatError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/countryCode")))),
        (
          "AA123456A",
          "XAIS12345678910",
          "2021-22",
          bothAllowancesSuppliedErrorRequestJson,
          BAD_REQUEST,
          RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("/foreignFhlEea/allowances")))),
        (
          "AA123456A",
          "XAIS12345678910",
          "2022-23",
          ruleBuildingNameOrNumberErrorRequestJson,
          BAD_REQUEST,
          RuleBuildingNameNumberError.copy(paths = Some(Seq("/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building"))))
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

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
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
        (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_FAILURE", BAD_REQUEST, RulePropertyIncomeAllowanceError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "MISSING_ALLOWANCES", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", BAD_REQUEST, RuleDuplicateCountryCodeError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = List(
        (UNPROCESSABLE_ENTITY, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "FIELD_CONFLICT", BAD_REQUEST, RulePropertyIncomeAllowanceError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

  private trait Test {

    val nino: String          = "TC663795B"
    val businessId: String    = "XAIS12345678910"
    val correlationId: String = "X-123"

    def setupStubs(): StubMapping

    def taxYear: String
    def downstreamUri: String
    def downstreamQueryParams: Map[String, String]

    def mtdUri: String = s"/foreign/$nino/$businessId/annual/$taxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links":[
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/annual/$taxYear",
         |      "method":"PUT",
         |      "rel":"create-and-amend-foreign-property-annual-submission"
         |    },
         |    {
         |      "href":"/individuals/business/property/foreign/$nino/$businessId/annual/$taxYear",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/$nino/$businessId/annual/$taxYear",
         |      "method":"DELETE",
         |      "rel":"delete-property-annual-submission"
         |    }
         |  ]
         |}
      """.stripMargin
    )

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "downstream error message"
         |}
       """.stripMargin

  }

  private trait TysIfsTest extends Test {
    def taxYear: String                            = "2023-24"
    def downstreamUri: String                      = s"/income-tax/business/property/annual/23-24/$nino/$businessId"
    def downstreamQueryParams: Map[String, String] = Map()
  }

  private trait NonTysTest extends Test {
    def taxYear: String       = "2022-23"
    def downstreamUri: String = s"/income-tax/business/property/annual"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> "2022-23"
    )

  }

}
