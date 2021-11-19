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

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{ JsObject, JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.V2IntegrationBaseSpec
import v2.models.errors._
import v2.stubs.{ AuthStub, IfsStub, MtdIdLookupStub }

class AmendUkPropertyAnnualSubmissionControllerISpec extends V2IntegrationBaseSpec {

  private trait Test {

    val nino: String = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val taxYear: String = "2022-23"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "ukFhlProperty": {
        |    "allowances": {
        |      "annualInvestmentAllowance": 1000.50,
        |      "businessPremisesRenovationAllowance": 1000.60,
        |      "otherCapitalAllowance": 1000.70,
        |      "electricChargePointAllowance": 1000.80,
        |      "zeroEmissionsCarAllowance": 1000.90
        |    },
        |    "adjustments": {
        |      "lossBroughtForward": 1000.10,
        |      "privateUseAdjustment": 1000.20,
        |      "balancingCharge": 1000.30,
        |      "periodOfGraceAdjustment": true,
        |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
        |      "nonResidentLandlord": true,
        |      "rentARoom": {
        |        "jointlyLet": true
        |      }
        |    }
        |  },
        |  "ukNonFhlProperty": {
        |    "allowances": {
        |      "annualInvestmentAllowance": 2000.50,
        |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
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
        |      "lossBroughtForward": 2000.10,
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

    val responseBody: JsValue = Json.parse(
      """
        |{
        |  "links":[
        |    {
        |      "href":"/individuals/business/property/uk/TC663795B/XAIS12345678910/annual/2022-23",
        |      "method":"PUT",
        |      "rel":"amend-uk-property-annual-submission"
        |    },
        |    {
        |      "href":"/individuals/business/property/uk/TC663795B/XAIS12345678910/annual/2022-23",
        |      "method":"GET",
        |      "rel":"self"
        |    },
        |    {
        |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/annual/2022-23",
        |      "method":"DELETE",
        |      "rel":"delete-property-annual-submission"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String = s"/uk/$nino/$businessId/annual/$taxYear"

    def ifsUri: String = s"/income-tax/business/property/annual"

      def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId" -> businessId,
      "taxYear" -> taxYear
      )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin
  }

  "Calling the amend uk property annual submission endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          IfsStub.onSuccess(IfsStub.PUT, ifsUri, ifsQueryParams, NO_CONTENT, JsObject.empty)
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
            |  "ukFhlProperty": {
            |    "allowances": {
            |      "annualInvestmentAllowance": 1000.50678,
            |      "businessPremisesRenovationAllowance": 1000.65670,
            |      "otherCapitalAllowance": 1000.76780,
            |      "electricChargePointAllowance": 1000.856780,
            |      "zeroEmissionsCarAllowance": 1000.678990
            |    },
            |    "adjustments": {
            |      "lossBroughtForward": 1000.678910,
            |      "privateUseAdjustment": 1000.2456780,
            |      "balancingCharge": 1000.356780,
            |      "periodOfGraceAdjustment": true,
            |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.4567890,
            |      "nonResidentLandlord": true,
            |      "rentARoom": {
            |        "jointlyLet": true
            |      }
            |    }
            |  },
            |  "ukNonFhlProperty": {
            |    "allowances": {
            |      "annualInvestmentAllowance": 2000.5456780,
            |      "zeroEmissionGoodsVehicleAllowance": 2000.6567890,
            |      "businessPremisesRenovationAllowance": 2000.75678900,
            |      "otherCapitalAllowance": 2000.56789080,
            |      "costOfReplacingDomesticGoods": 2000.95678900,
            |      "electricChargePointAllowance": 3000.1567890,
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
            |      "lossBroughtForward": 2000.56789010,
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
          ValueFormatError.copy(
            paths = Some(List(
              "/ukFhlProperty/adjustments/lossBroughtForward",
              "/ukFhlProperty/adjustments/balancingCharge",
              "/ukFhlProperty/adjustments/privateUseAdjustment",
              "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
              "/ukFhlProperty/allowances/annualInvestmentAllowance",
              "/ukFhlProperty/allowances/businessPremisesRenovationAllowance",
              "/ukFhlProperty/allowances/otherCapitalAllowance",
              "/ukFhlProperty/allowances/electricChargePointAllowance",
              "/ukFhlProperty/allowances/zeroEmissionsCarAllowance",
              "/ukNonFhlProperty/adjustments/lossBroughtForward",
              "/ukNonFhlProperty/adjustments/balancingCharge",
              "/ukNonFhlProperty/adjustments/privateUseAdjustment",
              "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
              "/ukNonFhlProperty/allowances/annualInvestmentAllowance",
              "/ukNonFhlProperty/allowances/zeroEmissionGoodsVehicleAllowance",
              "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance",
              "/ukNonFhlProperty/allowances/otherCapitalAllowance",
              "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods",
              "/ukNonFhlProperty/allowances/electricChargePointAllowance",
              "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/amount",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/amount",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
            ))
          ),
          StringFormatError.copy(
            paths = Some(List(
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/name",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/postcode",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"
            ))
          ),
          DateFormatError.copy(
            paths = Some(List(
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"
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

      val validRequestBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 1000.50,
          |      "businessPremisesRenovationAllowance": 1000.60,
          |      "otherCapitalAllowance": 1000.70,
          |      "electricChargePointAllowance": 1000.80,
          |      "zeroEmissionsCarAllowance": 1000.90
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10,
          |      "privateUseAdjustment": 1000.20,
          |      "balancingCharge": 1000.30,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
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
          |      "lossBroughtForward": 2000.10,
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

      val allInvalidValueRequestBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 1000.7650,
          |      "businessPremisesRenovationAllowance": 1000.6350,
          |      "otherCapitalAllowance": 1000.72650,
          |      "electricChargePointAllowance": 1000.834650,
          |      "zeroEmissionsCarAllowance": 1000.923540
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10346,
          |      "privateUseAdjustment": 1000.22540,
          |      "balancingCharge": 1000.336540,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.4253420,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.5635430,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.6235450,
          |      "businessPremisesRenovationAllowance": 2000.745360,
          |      "otherCapitalAllowance": 2000.8243520,
          |      "costOfReplacingDomesticGoods": 2000.9064532,
          |      "electricChargePointAllowance": 3000.1234520,
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
          |      "lossBroughtForward": 2000.13460,
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

      val allInvalidDateFormatRequestBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 1000.50,
          |      "businessPremisesRenovationAllowance": 1000.60,
          |      "otherCapitalAllowance": 1000.70,
          |      "electricChargePointAllowance": 1000.80,
          |      "zeroEmissionsCarAllowance": 1000.90
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10,
          |      "privateUseAdjustment": 1000.20,
          |      "balancingCharge": 1000.30,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
          |      "businessPremisesRenovationAllowance": 2000.70,
          |      "otherCapitalAllowance": 2000.80,
          |      "costOfReplacingDomesticGoods": 2000.90,
          |      "electricChargePointAllowance": 3000.10,
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
          |      "lossBroughtForward": 2000.10,
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

      val allInvalidStringRequestBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 1000.50,
          |      "businessPremisesRenovationAllowance": 1000.60,
          |      "otherCapitalAllowance": 1000.70,
          |      "electricChargePointAllowance": 1000.80,
          |      "zeroEmissionsCarAllowance": 1000.90
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10,
          |      "privateUseAdjustment": 1000.20,
          |      "balancingCharge": 1000.30,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
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
          |      "lossBroughtForward": 2000.10,
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

      val buildingNameNumberBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 1000.50,
          |      "businessPremisesRenovationAllowance": 1000.60,
          |      "otherCapitalAllowance": 1000.70,
          |      "electricChargePointAllowance": 1000.80,
          |      "zeroEmissionsCarAllowance": 1000.90
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10,
          |      "privateUseAdjustment": 1000.20,
          |      "balancingCharge": 1000.30,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "annualInvestmentAllowance": 2000.50,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
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
          |      "lossBroughtForward": 2000.10,
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

      val bothAllowancesSuppliedBodyJson = Json.parse(
        """
          |{
          |  "ukFhlProperty": {
          |    "allowances": {
          |      "propertyIncomeAllowance": 3456.76,
          |      "annualInvestmentAllowance": 1000.50,
          |      "businessPremisesRenovationAllowance": 1000.60,
          |      "otherCapitalAllowance": 1000.70,
          |      "electricChargePointAllowance": 1000.80,
          |      "zeroEmissionsCarAllowance": 1000.90
          |    },
          |    "adjustments": {
          |      "lossBroughtForward": 1000.10,
          |      "privateUseAdjustment": 1000.20,
          |      "balancingCharge": 1000.30,
          |      "periodOfGraceAdjustment": true,
          |      "businessPremisesRenovationAllowanceBalancingCharges": 1000.40,
          |      "nonResidentLandlord": true,
          |      "rentARoom": {
          |        "jointlyLet": true
          |      }
          |    }
          |  },
          |  "ukNonFhlProperty": {
          |    "allowances": {
          |      "propertyIncomeAllowance": 3456.76,
          |      "annualInvestmentAllowance": 2000.50,
          |      "zeroEmissionGoodsVehicleAllowance": 2000.60,
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
          |      "lossBroughtForward": 2000.10,
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

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "One or more monetary fields are invalid",
        paths = Some(List(
          "/ukFhlProperty/adjustments/lossBroughtForward",
          "/ukFhlProperty/adjustments/balancingCharge",
          "/ukFhlProperty/adjustments/privateUseAdjustment",
          "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
          "/ukFhlProperty/allowances/annualInvestmentAllowance",
          "/ukFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukFhlProperty/allowances/otherCapitalAllowance",
          "/ukFhlProperty/allowances/electricChargePointAllowance",
          "/ukFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukNonFhlProperty/adjustments/lossBroughtForward",
          "/ukNonFhlProperty/adjustments/balancingCharge",
          "/ukNonFhlProperty/adjustments/privateUseAdjustment",
          "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
          "/ukNonFhlProperty/allowances/annualInvestmentAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionGoodsVehicleAllowance",
          "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukNonFhlProperty/allowances/otherCapitalAllowance",
          "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods",
          "/ukNonFhlProperty/allowances/electricChargePointAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/amount",
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/amount",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
        ))
      )

      val allInvalidDateFormatRequestError: MtdError = DateFormatError.copy(
        message = "The supplied date format is not valid",
        paths = Some(List(
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"
        ))
      )

      val allInvalidStringRequestError: MtdError = StringFormatError.copy(
        message = "The supplied string format is not valid",
        paths = Some(List(
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/name",
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building/postcode",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"
        ))
      )

      val buildingNameNumberError: MtdError = RuleBuildingNameNumberError.copy(
        message = "Postcode must be supplied along with at least one of name or number",
        paths = Some(List(
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building"
        ))
      )

      val bothAllowancesSuppliedError: MtdError = RuleBothAllowancesSuppliedError.copy(
        message = "Both allowances and property allowances must not be present at the same time",
        paths = Some(List(
          "/ukFhlProperty/allowances",
          "/ukNonFhlProperty/allowances"
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
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "XAIS12345678910", "2022-23", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XAIS12345678910", "202362-23", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS1234dfxgchjbn5678910", "2022-23", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2021-24", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2021-22", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XAIS12345678910", "2022-23", Json.parse(s"""{}""".stripMargin), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "XAIS12345678910", "2022-23", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", allInvalidDateFormatRequestBodyJson, BAD_REQUEST, allInvalidDateFormatRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", allInvalidStringRequestBodyJson, BAD_REQUEST, allInvalidStringRequestError),
          ("AA123456A", "XAIS12345678910", "2022-23", buildingNameNumberBodyJson, BAD_REQUEST, buildingNameNumberError),
          ("AA123456A", "XAIS12345678910", "2022-23", bothAllowancesSuppliedBodyJson, BAD_REQUEST, bothAllowancesSuppliedError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(ifsStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              IfsStub.onError(IfsStub.PUT, ifsUri, ifsStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_PAYLOAD", BAD_REQUEST, RuleTypeOfBusinessIncorrectError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_COUNTRY_CODE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}