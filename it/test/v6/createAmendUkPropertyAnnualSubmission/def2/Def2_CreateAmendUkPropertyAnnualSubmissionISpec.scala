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

package v6.createAmendUkPropertyAnnualSubmission.def2

import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.{
  RuleBothAllowancesSuppliedError,
  RuleBuildingNameNumberError,
  RuleOutsideAmendmentWindowError,
  RulePropertyIncomeAllowanceError,
  RuleTypeOfBusinessIncorrectError
}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.support.IntegrationBaseSpec

class Def2_CreateAmendUkPropertyAnnualSubmissionISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino: String          = "TC663795B"
    val businessId: String    = "XAIS12345678910"
    val correlationId: String = "X-123"

    def taxYear: String
    def downstreamTaxYear: String
    def downstreamUri: String

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "ukProperty": {
        |    "allowances": {
        |      "annualInvestmentAllowance": 2000.50,
        |      "businessPremisesRenovationAllowance": 2000.70,
        |      "otherCapitalAllowance": 2000.80,
        |      "costOfReplacingDomesticGoods": 2000.90,
        |      "electricChargePointAllowance": 3000.10,
        |      "structuredBuildingAllowance": [
        |        {
        |          "amount": 3000.30,
        |          "firstYear": {
        |            "qualifyingDate": "2020-01-01",
        |            "qualifyingAmountExpenditure": 3000.40
        |          },
        |          "building": {
        |            "name": "house name",
        |            "postcode": "GF49JH"
        |          }
        |        }
        |      ],
        |      "enhancedStructuredBuildingAllowance": [
        |        {
        |          "amount": 3000.50,
        |          "firstYear": {
        |            "qualifyingDate": "2020-01-01",
        |            "qualifyingAmountExpenditure": 3000.60
        |          },
        |          "building": {
        |            "number": "house number",
        |            "postcode": "GF49JH"
        |          }
        |        }
        |      ],
        |      "zeroEmissionsCarAllowance": 3000.20
        |    },
        |    "adjustments": {
        |      "balancingCharge": 2000.20,
        |      "privateUseAdjustment": 2000.30,
        |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
        |      "nonResidentLandlord": true,
        |      "rentARoom": {
        |        "jointlyLet": true
        |      }
        |    }
        |  }
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String = s"/uk/$nino/$businessId/annual/$taxYear"

    def baseUri: String = s"/income-tax/business/property/annual"

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

    override def downstreamUri: String = baseUri + s"/$downstreamTaxYear/$nino/$businessId"
  }

  "Calling the amend uk property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made with a Tax Year Specific tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return a 400 with multiple errors" when {
      "all field validations fail on the request body" in new TysIfsTest {

        val allInvalidFieldsRequestBodyJson: JsValue = Json.parse("""
            |{
            |  "ukProperty": {
            |    "allowances": {
            |      "annualInvestmentAllowance": 2000.5456780,
            |      "businessPremisesRenovationAllowance": 2000.75678900,
            |      "otherCapitalAllowance": 2000.56789080,
            |      "costOfReplacingDomesticItems": 2000.95678900,
            |      "structuredBuildingAllowance": [
            |        {
            |          "amount": 3000.36780,
            |          "firstYear": {
            |            "qualifyingDate": "202456780-01-01",
            |            "qualifyingAmountExpenditure": 3000.45678900
            |          },
            |          "building": {
            |            "name": "house name*",
            |            "postcode": "GF49JH*"
            |          }
            |        }
            |      ],
            |      "enhancedStructuredBuildingAllowance": [
            |        {
            |          "amount": 3000.5345670,
            |          "firstYear": {
            |            "qualifyingDate": "24568020-01-01",
            |            "qualifyingAmountExpenditure": 3000.64567890
            |          },
            |          "building": {
            |            "number": "house number*",
            |            "postcode": "GF49JH*"
            |          }
            |        }
            |      ],
            |      "zeroEmissionsCarAllowance": 3000.45678920
            |    },
            |    "adjustments": {
            |      "balancingCharge": 2000.6789020,
            |      "privateUseAdjustment": 2000.34567890,
            |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.456780,
            |      "nonResidentLandlord": true,
            |      "rentARoom": {
            |        "jointlyLet": true
            |      }
            |    }
            |  }
            |}
            |""".stripMargin)

        val allInvalidFieldsRequestError: List[MtdError] = List(
          DateFormatError.copy(
            paths = Some(
              List(
                "/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate",
                "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"
              ))
          ),
          StringFormatError.copy(
            paths = Some(
              List(
                "/ukProperty/allowances/structuredBuildingAllowance/0/building/name",
                "/ukProperty/allowances/structuredBuildingAllowance/0/building/postcode",
                "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number",
                "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"
              ))
          ),
          ValueFormatError.copy(
            paths = Some(
              List(
                "/ukProperty/adjustments/balancingCharge",
                "/ukProperty/adjustments/privateUseAdjustment",
                "/ukProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
                "/ukProperty/allowances/annualInvestmentAllowance",
                "/ukProperty/allowances/businessPremisesRenovationAllowance",
                "/ukProperty/allowances/otherCapitalAllowance",
                "/ukProperty/allowances/costOfReplacingDomesticItems",
                "/ukProperty/allowances/zeroEmissionsCarAllowance",
                "/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
                "/ukProperty/allowances/structuredBuildingAllowance/0/amount",
                "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
                "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/amount"
              ))
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

      val validRequestBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.30,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.40
          |          },
          |          "building": {
          |            "name": "house name",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.50,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.60
          |          },
          |          "building": {
          |            "number": "house number",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.20
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.20,
          |      "privateUseAdjustment": 2000.30,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val allInvalidValueRequestBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.5635430,
          |      "businessPremisesRenovationAllowance": 2000.745360,
          |      "otherCapitalAllowance": 2000.8243520,
          |      "costOfReplacingDomesticItems": 2000.9064532,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.303645,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.4234520
          |          },
          |          "building": {
          |            "name": "house name",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.534560,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.656430
          |          },
          |          "building": {
          |            "number": "house number",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.2364560
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.26530,
          |      "privateUseAdjustment": 2000.2645230,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.434560,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val allInvalidDateFormatRequestBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.30,
          |          "firstYear": {
          |            "qualifyingDate": "2034620-01-01",
          |            "qualifyingAmountExpenditure": 3000.40
          |          },
          |          "building": {
          |            "name": "house name",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.50,
          |          "firstYear": {
          |            "qualifyingDate": "2031420-01-01",
          |            "qualifyingAmountExpenditure": 3000.60
          |          },
          |          "building": {
          |            "number": "house number",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.20
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.20,
          |      "privateUseAdjustment": 2000.30,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val allInvalidStringRequestBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.30,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.40
          |          },
          |          "building": {
          |            "name": "house name*",
          |            "postcode": "GF49JH*"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.50,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.60
          |          },
          |          "building": {
          |            "number": "house number*",
          |            "postcode": "GF49JH*"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.20
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.20,
          |      "privateUseAdjustment": 2000.30,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val buildingNameNumberBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.30,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.40
          |          },
          |          "building": {
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.50,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.60
          |          },
          |          "building": {
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.20
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.20,
          |      "privateUseAdjustment": 2000.30,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val bothAllowancesSuppliedBodyJson = Json.parse("""
          |{
          |  "ukProperty": {
          |    "allowances": {
          |      "propertyIncomeAllowance": 1000.00,
          |      "annualInvestmentAllowance": 2000.50,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "structuredBuildingAllowance": [
          |        {
          |          "amount": 3000.30,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.40
          |          },
          |          "building": {
          |            "name": "house name",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "enhancedStructuredBuildingAllowance": [
          |        {
          |          "amount": 3000.50,
          |          "firstYear": {
          |            "qualifyingDate": "2020-01-01",
          |            "qualifyingAmountExpenditure": 3000.60
          |          },
          |          "building": {
          |            "number": "house number",
          |            "postcode": "GF49JH"
          |          }
          |        }
          |      ],
          |      "zeroEmissionsCarAllowance": 3000.20
          |    },
          |    "adjustments": {
          |      "balancingCharge": 2000.20,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "The value must be between 0 and 99999999999.99",
        paths = Some(
          List(
            "/ukProperty/adjustments/balancingCharge",
            "/ukProperty/adjustments/privateUseAdjustment",
            "/ukProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
            "/ukProperty/allowances/annualInvestmentAllowance",
            "/ukProperty/allowances/businessPremisesRenovationAllowance",
            "/ukProperty/allowances/otherCapitalAllowance",
            "/ukProperty/allowances/costOfReplacingDomesticItems",
            "/ukProperty/allowances/zeroEmissionsCarAllowance",
            "/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
            "/ukProperty/allowances/structuredBuildingAllowance/0/amount",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/amount"
          ))
      )

      val allInvalidDateFormatRequestError: MtdError = DateFormatError.copy(
        message = "The supplied date format is not valid",
        paths = Some(
          List(
            "/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"
          ))
      )

      val allInvalidStringRequestError: MtdError = StringFormatError.copy(
        message = "The supplied string format is not valid",
        paths = Some(
          List(
            "/ukProperty/allowances/structuredBuildingAllowance/0/building/name",
            "/ukProperty/allowances/structuredBuildingAllowance/0/building/postcode",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"
          ))
      )

      val buildingNameNumberError: MtdError = RuleBuildingNameNumberError.copy(
        message = "Postcode must be supplied along with at least one of name or number",
        paths = Some(
          List(
            "/ukProperty/allowances/structuredBuildingAllowance/0/building",
            "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building"
          ))
      )

      val bothAllowancesSuppliedError: MtdError = RuleBothAllowancesSuppliedError.copy(
        message = "Both allowances and property allowances must not be present at the same time",
        paths = Some(
          List(
            "/ukProperty/allowances"
          ))
      )

      "validation error occurs" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new TysIfsTest {

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
          ("AA1123A", "XAIS12345678910", "2025-26", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "202362-23", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS1234dfxgchjbn5678910", "2025-26", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2025-28", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2025-26", Json.parse(s"""{}""".stripMargin), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "XAIS12345678910", "2025-26", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError),
          ("AA123456A", "XAIS12345678910", "2025-26", allInvalidDateFormatRequestBodyJson, BAD_REQUEST, allInvalidDateFormatRequestError),
          ("AA123456A", "XAIS12345678910", "2025-26", allInvalidStringRequestBodyJson, BAD_REQUEST, allInvalidStringRequestError),
          ("AA123456A", "XAIS12345678910", "2025-26", buildingNameNumberBodyJson, BAD_REQUEST, buildingNameNumberError),
          ("AA123456A", "XAIS12345678910", "2025-26", bothAllowancesSuppliedBodyJson, BAD_REQUEST, bothAllowancesSuppliedError)
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
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_FAILURE", BAD_REQUEST, RulePropertyIncomeAllowanceError),
          (UNPROCESSABLE_ENTITY, "MISSING_ALLOWANCES", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError)
        )

        val extraTysErrors = List(
          (INTERNAL_SERVER_ERROR, "MISSING_EXPENSES", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "FIELD_CONFLICT", BAD_REQUEST, RulePropertyIncomeAllowanceError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
