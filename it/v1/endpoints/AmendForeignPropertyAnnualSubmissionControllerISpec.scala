/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendForeignPropertyAnnualSubmissionControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "TC663795B"
    val businessId = "XAIS12345678910"
    val taxYear = "2021-22"
    val correlationId = "X-123"

    val requestBodyJson = Json.parse(
      """
        |{
        |  "foreignFhlEea": {
        |      "adjustments": {
        |        "privateUseAdjustment":100.25,
        |        "balancingCharge":100.25,
        |        "periodOfGraceAdjustment":true
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance":100.25,
        |        "otherCapitalAllowance":100.25,
        |        "propertyAllowance":100.25,
        |        "electricChargePointAllowance":100.25
        |      }
        |    },
        |  "foreignProperty": [
        |    {
        |      "countryCode":"FRA",
        |      "adjustments": {
        |        "privateUseAdjustment":100.25,
        |        "balancingCharge":100.25
        |      },
        |      "allowances": {
        |        "annualInvestmentAllowance":100.25,
        |        "costOfReplacingDomesticItems":100.25,
        |        "zeroEmissionsGoodsVehicleAllowance":100.25,
        |        "propertyAllowance":100.25,
        |        "otherCapitalAllowance":100.25,
        |        "structureAndBuildingAllowance":100.25,
        |        "electricChargePointAllowance":100.25
        |      }
        |    }
        |  ]
        |}
        |""".stripMargin
    )


    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links":[
         |    {
         |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/annual/2021-22",
         |      "method":"GET",
         |      "rel":"self"
         |    },
         |    {
         |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/annual/2021-22",
         |      "method":"PUT",
         |      "rel":"amend-property-annual-submission"
         |    },
         |    {
         |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/annual/2021-22",
         |      "method":"DELETE",
         |      "rel":"delete-property-annual-submission"
         |    }
         |  ]
         |}
         |""".stripMargin)

    def setupStubs(): StubMapping

    def uri: String = s"/$nino/$businessId/annual/$taxYear"

    def desUri: String = s"/business/property/$nino/$businessId/annual/$taxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the amend foreign property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field validations fail on the request body" in new Test {

        val allInvalidFieldsRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |      "adjustments": {
            |        "privateUseAdjustment":100.256436,
            |        "balancingCharge":100.256436,
            |        "periodOfGraceAdjustment":true
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.256436,
            |        "otherCapitalAllowance":100.256436,
            |        "propertyAllowance":100.256436,
            |        "electricChargePointAllowance":100.256436
            |      }
            |    },
            |  "foreignProperty": [
            |    {
            |      "countryCode":"AHHHHHH",
            |      "adjustments": {
            |        "privateUseAdjustment":100.256436,
            |        "balancingCharge":100.256436
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.256436,
            |        "costOfReplacingDomesticItems":100.256436,
            |        "zeroEmissionsGoodsVehicleAllowance":100.256436,
            |        "propertyAllowance":100.256436,
            |        "otherCapitalAllowance":100.256436,
            |        "structureAndBuildingAllowance":100.256436,
            |        "electricChargePointAllowance":100.256436
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allInvalidFieldsRequestError: List[MtdError] = List(
          CountryCodeFormatError.copy(
            message = "The provided Country code is invalid",
            paths = Some(List(
              "/foreignProperty/0/countryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "One or more monetary fields are invalid",
            paths = Some(List(
              "/foreignFhlEea/adjustments/privateUseAdjustment",
              "/foreignFhlEea/adjustments/balancingCharge",
              "/foreignFhlEea/allowances/annualInvestmentAllowance",
              "/foreignFhlEea/allowances/otherCapitalAllowance",
              "/foreignFhlEea/allowances/propertyAllowance",
              "/foreignFhlEea/allowances/electricChargePointAllowance",
              "/foreignProperty/0/adjustments/privateUseAdjustment",
              "/foreignProperty/0/adjustments/balancingCharge",
              "/foreignProperty/0/allowances/annualInvestmentAllowance",
              "/foreignProperty/0/allowances/costOfReplacingDomesticItems",
              "/foreignProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance",
              "/foreignProperty/0/allowances/propertyAllowance",
              "/foreignProperty/0/allowances/otherCapitalAllowance",
              "/foreignProperty/0/allowances/structureAndBuildingAllowance",
              "/foreignProperty/0/allowances/electricChargePointAllowance"
            ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidFieldsRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidFieldsRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "return an error according to spec" when {

        val validRequestBodyJson = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |      "adjustments": {
            |        "privateUseAdjustment":100.25,
            |        "balancingCharge":100.25,
            |        "periodOfGraceAdjustment":true
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.25,
            |        "otherCapitalAllowance":100.25,
            |        "propertyAllowance":100.25,
            |        "electricChargePointAllowance":100.25
            |      }
            |    },
            |  "foreignProperty": [
            |    {
            |      "countryCode":"FRA",
            |      "adjustments": {
            |        "privateUseAdjustment":100.25,
            |        "balancingCharge":100.25
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.25,
            |        "costOfReplacingDomesticItems":100.25,
            |        "zeroEmissionsGoodsVehicleAllowance":100.25,
            |        "propertyAllowance":100.25,
            |        "otherCapitalAllowance":100.25,
            |        "structureAndBuildingAllowance":100.25,
            |        "electricChargePointAllowance":100.25
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |      "adjustments": {
            |        "privateUseAdjustment":100.256436,
            |        "balancingCharge":100.256436,
            |        "periodOfGraceAdjustment":true
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.256436,
            |        "otherCapitalAllowance":100.256436,
            |        "propertyAllowance":100.256436,
            |        "electricChargePointAllowance":100.256436
            |      }
            |    },
            |  "foreignProperty": [
            |    {
            |      "countryCode":"FRA",
            |      "adjustments": {
            |        "privateUseAdjustment":100.256436,
            |        "balancingCharge":100.256436
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.256436,
            |        "costOfReplacingDomesticItems":100.256436,
            |        "zeroEmissionsGoodsVehicleAllowance":100.256436,
            |        "propertyAllowance":100.256436,
            |        "otherCapitalAllowance":100.256436,
            |        "structureAndBuildingAllowance":100.256436,
            |        "electricChargePointAllowance":100.256436
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allInvalidCountryCodeRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "foreignFhlEea": {
            |      "adjustments": {
            |        "privateUseAdjustment":100.25,
            |        "balancingCharge":100.25,
            |        "periodOfGraceAdjustment":true
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.25,
            |        "otherCapitalAllowance":100.25,
            |        "propertyAllowance":100.25,
            |        "electricChargePointAllowance":100.25
            |      }
            |    },
            |  "foreignProperty": [
            |    {
            |      "countryCode":"AAAAAAAA",
            |      "adjustments": {
            |        "privateUseAdjustment":100.25,
            |        "balancingCharge":100.25
            |      },
            |      "allowances": {
            |        "annualInvestmentAllowance":100.25,
            |        "costOfReplacingDomesticItems":100.25,
            |        "zeroEmissionsGoodsVehicleAllowance":100.25,
            |        "propertyAllowance":100.25,
            |        "otherCapitalAllowance":100.25,
            |        "structureAndBuildingAllowance":100.25,
            |        "electricChargePointAllowance":100.25
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
          message = "One or more monetary fields are invalid",
          paths = Some(List(
            "/foreignFhlEea/adjustments/privateUseAdjustment",
            "/foreignFhlEea/adjustments/balancingCharge",
            "/foreignFhlEea/allowances/annualInvestmentAllowance",
            "/foreignFhlEea/allowances/otherCapitalAllowance",
            "/foreignFhlEea/allowances/propertyAllowance",
            "/foreignFhlEea/allowances/electricChargePointAllowance",
            "/foreignProperty/0/adjustments/privateUseAdjustment",
            "/foreignProperty/0/adjustments/balancingCharge",
            "/foreignProperty/0/allowances/annualInvestmentAllowance",
            "/foreignProperty/0/allowances/costOfReplacingDomesticItems",
            "/foreignProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance",
            "/foreignProperty/0/allowances/propertyAllowance",
            "/foreignProperty/0/allowances/otherCapitalAllowance",
            "/foreignProperty/0/allowances/structureAndBuildingAllowance",
            "/foreignProperty/0/allowances/electricChargePointAllowance"
          ))
        )

        val allInvalidCountryCodeRequestError: MtdError = CountryCodeFormatError.copy(
          message = "The provided Country code is invalid",
          paths = Some(List(
            "/foreignProperty/0/countryCode"
          ))
        )

        "validation error occurs" when {
          def validationErrorTest(requestNino: String,
                                  requestBusinessId: String,
                                  requestTaxYear: String,
                                  requestBody: JsValue,
                                  expectedStatus: Int,
                                  expectedBody: MtdError): Unit = {
            s"validation fails with ${expectedBody.code} error" in new Test {

              override val nino: String = requestNino
              override val businessId: String = requestBusinessId
              override val taxYear: String = requestTaxYear
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

          val input = Seq(
            ("AA1123A", "XAIS12345678910", "2021-22", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
            ("AA123456A", "XAIS1234dfxgchjbn5678910", "2021-22", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError),
            ("AA123456A", "XAIS12345678910", "2021-24", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
            ("AA123456A", "XAIS12345678910", "2019-20", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
            ("AA123456A", "XAIS12345678910", "2021-22",
              Json.parse(s"""{"foreignFhlEea": 2342314}""".stripMargin), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
            ("AA123456A", "XAIS12345678910", "2021-22", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError),
            ("AA123456A", "XAIS12345678910", "2021-22", allInvalidCountryCodeRequestBodyJson, BAD_REQUEST, allInvalidCountryCodeRequestError)
          )

          input.foreach(args => (validationErrorTest _).tupled(args))
        }

        "des service error" when {
          def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
            s"des returns an $desCode error and status $desStatus" in new Test {

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                AuthStub.authorised()
                MtdIdLookupStub.ninoFound(nino)
                DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
              }

              val response: WSResponse = await(request().put(requestBodyJson))
              response.status shouldBe expectedStatus
              response.json shouldBe Json.toJson(expectedBody)
            }
          }

          val input = Seq(
            (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
            (BAD_REQUEST, "INVALID_INCOME_SOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
            (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
            (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
            (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

          input.foreach(args => (serviceErrorTest _).tupled(args))
        }
      }
    }
  }
}
