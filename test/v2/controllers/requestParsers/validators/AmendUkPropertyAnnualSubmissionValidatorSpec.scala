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

package v2.controllers.requestParsers.validators

import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v2.models.errors._
import v2.models.request.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionRawData
import v2.models.utils.JsonErrorValidators

class AmendUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val validNino = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear = "2021-22"
  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "ukFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 123.45,
      |      "businessPremisesRenovationAllowance": 345.56,
      |      "otherCapitalAllowance": 345.34,
      |      "electricChargePointAllowance": 453.34,
      |      "zeroEmissionsCarAllowance": 123.12
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 343.34,
      |      "privateUseAdjustment": 454.45,
      |      "balancingCharge": 231.45,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 678.45,
      |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
      |      "businessPremisesRenovationAllowance": 573.45,
      |      "otherCapitalAllowance": 452.34,
      |      "costOfReplacingDomesticGoods": 567.34,
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
      |      "lossBroughtForward": 334.45,
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
      |""".stripMargin)


  MockAppConfig.minimumTaxV2Uk returns 2021
  val validator = new AmendUkPropertyAnnualSubmissionValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, requestBodyJson)) shouldBe Nil
      }

      "a minimal request is supplied" in {
        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
                |{
                |  "ukFhlProperty": {
                |    "adjustments": {
                |      "periodOfGraceAdjustment": true,
                |      "nonResidentLandlord": true
                |    }
                |  }
                |}
                |""".stripMargin)
          )
        ) shouldBe Nil
      }

      "only a ukFhlProperty is supplied" in {
        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
                |{
                |  "ukFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 123.45,
                |      "businessPremisesRenovationAllowance": 345.56,
                |      "otherCapitalAllowance": 345.34,
                |      "electricChargePointAllowance": 453.34,
                |      "zeroEmissionsCarAllowance": 123.12
                |    },
                |    "adjustments": {
                |      "lossBroughtForward": 343.34,
                |      "privateUseAdjustment": 454.45,
                |      "balancingCharge": 231.45,
                |      "periodOfGraceAdjustment": true,
                |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
                |      "nonResidentLandlord": true,
                |      "rentARoom": {
                |        "jointlyLet": true
                |      }
                |    }
                |  }
                |}
                |""".stripMargin)
          )
        ) shouldBe Nil
      }

      "only a ukNonFhlProperty is supplied" in {
        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
                |{
                |  "ukNonFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 678.45,
                |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
                |      "businessPremisesRenovationAllowance": 573.45,
                |      "otherCapitalAllowance": 452.34,
                |      "costOfReplacingDomesticGoods": 567.34,
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
                |      "lossBroughtForward": 334.45,
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
                |
                |""".stripMargin)
          )
        ) shouldBe Nil
      }
    }

    "return a path parameter error" when {
      "an invalid nino is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData("A12344A", validBusinessId, validTaxYear, requestBodyJson)) shouldBe List(
          NinoFormatError
        )
      }

      "an invalid businessId is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, "Walrus", validTaxYear, requestBodyJson)) shouldBe List(
          BusinessIdFormatError)
      }

      "an invalid taxYear is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2103/01", requestBodyJson)) shouldBe List(
          TaxYearFormatError)
      }

      "an invalid taxYear range is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2022-24", requestBodyJson)) shouldBe List(
          RuleTaxYearRangeInvalidError)
      }

      "a below minimum taxYear is supplied" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2018-19", requestBodyJson)) shouldBe List(
          RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse("""{}"""))) shouldBe List(
          RuleIncorrectOrEmptyBodyError
        )
      }

      "an empty object is submitted" when {
        Seq(
          "/ukFhlProperty",
          "/ukFhlProperty/allowances",
          "/ukNonFhlProperty",
          "/ukNonFhlProperty/allowances",
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance",
        ).foreach(p => testEmpty(p))


        def testEmpty(path: String): Unit =
          s"for $path" in {
            validator.validate(
              AmendUkPropertyAnnualSubmissionRawData(
                validNino,
                validBusinessId,
                validTaxYear,
                requestBodyJson.removeProperty(path).update(path, JsObject.empty)
              )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(path))))
          }
      }

      "an object is empty except for a additional (non-schema) property" in {
        val json = Json.parse("""{
                                |    "ukFhlProperty":{
                                |       "unknownField": 999.99
                                |    }
                                |}""".stripMargin)

        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            json
          )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/ukFhlProperty"))))
      }

      "an empty ukFhlProperty adjustments object is submitted" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |      "adjustments": {}
              |  }
              |}
              |""".stripMargin)
        )) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/ukFhlProperty/adjustments/periodOfGraceAdjustment", "/ukFhlProperty/adjustments/nonResidentLandlord"))))
      }

      "an empty ukFhlProperty adjustments rentARoom object is submitted" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/ukFhlProperty/adjustments/rentARoom/jointlyLet")))
        )
      }

      "an empty ukNonFhlProperty adjustments object is submitted" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |    }
              |  }
              |}
              |
              |""".stripMargin)
        )) shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/ukNonFhlProperty/adjustments/nonResidentLandlord")))
        )
      }

      "an empty ukNonFhlProperty adjustments rentARoom object is submitted" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |      "lossBroughtForward": 334.45,
              |      "balancingCharge": 565.34,
              |      "privateUseAdjustment": 533.54,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 563.34,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |      }
              |    }
              |  }
              |}
              |""".stripMargin)
        )) shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/ukNonFhlProperty/adjustments/rentARoom/jointlyLet")))
        )
      }
    }

    "return Date Errors" when {
      "structuredBuildingAllowance/qualifyingDate is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
              |      "electricChargePointAllowance": 454.34,
              |      "structuredBuildingAllowance": [
              |        {
              |          "amount": 234.34,
              |          "firstYear": {
              |            "qualifyingDate": "2020.10.01",
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
              |            "qualifyingDate": "2020-03-29",
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
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(DateFormatError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"))))
      }

      "enhancedStructuredBuildingAllowance/qualifyingDate is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |            "qualifyingDate": "2020.10.01",
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
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(DateFormatError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate"))))
      }
    }

    "return String Errors" when {
      "structuredBuildingAllowance/building/name is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |            "name": "*",
              |            "number": "2",
              |            "postcode": "TF3 4ER"
              |          }
              |        }
              |      ],
              |      "zeroEmissionsCarAllowance": 454.34
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(StringFormatError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/name"))))
      }

      "structuredBuildingAllowance/building/number is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |            "number": "",
              |            "postcode": "TF3 4ER"
              |          }
              |        }
              |      ],
              |      "zeroEmissionsCarAllowance": 454.34
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(StringFormatError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number"))))
      }

      "structuredBuildingAllowance/building/postcode is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 123.45,
              |      "businessPremisesRenovationAllowance": 345.56,
              |      "otherCapitalAllowance": 345.34,
              |      "electricChargePointAllowance": 453.34,
              |      "zeroEmissionsCarAllowance": 123.12
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "privateUseAdjustment": 454.45,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |            "postcode": "*"
              |          }
              |        }
              |      ],
              |      "zeroEmissionsCarAllowance": 454.34
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(StringFormatError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode"))))
      }
    }

    "return ValueFormatError" when {
      "numeric fields are invalid" when {

        Seq(
          "/ukFhlProperty/allowances/annualInvestmentAllowance",
          "/ukFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukFhlProperty/allowances/otherCapitalAllowance",
          "/ukFhlProperty/allowances/electricChargePointAllowance",
          "/ukFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukFhlProperty/adjustments/lossBroughtForward",
          "/ukFhlProperty/adjustments/privateUseAdjustment",
          "/ukFhlProperty/adjustments/balancingCharge",
          "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
          "/ukNonFhlProperty/allowances/annualInvestmentAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
          "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukNonFhlProperty/allowances/otherCapitalAllowance",
          "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods",
          "/ukNonFhlProperty/allowances/electricChargePointAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukNonFhlProperty/adjustments/lossBroughtForward",
          "/ukNonFhlProperty/adjustments/balancingCharge",
          "/ukNonFhlProperty/adjustments/privateUseAdjustment",
          "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
        ).foreach(p => testValueFormatError(p))

        def testValueFormatError(path: String): Unit = s"for $path" in {
          validator.validate(
            AmendUkPropertyAnnualSubmissionRawData(
              validNino,
              validBusinessId,
              validTaxYear,
              requestBodyJson.update(path, JsNumber(123.456))
            )
          ) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
        }
      }

      "fields inside structuredBuildingAllowance are invalid" in {
        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
                |{
                |  "ukFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 123.45,
                |      "businessPremisesRenovationAllowance": 345.56,
                |      "otherCapitalAllowance": 345.34,
                |      "electricChargePointAllowance": 453.34,
                |      "zeroEmissionsCarAllowance": 123.12
                |    },
                |    "adjustments": {
                |      "lossBroughtForward": 343.34,
                |      "privateUseAdjustment": 454.45,
                |      "balancingCharge": 231.45,
                |      "periodOfGraceAdjustment": true,
                |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
                |      "nonResidentLandlord": true,
                |      "rentARoom": {
                |        "jointlyLet": true
                |      }
                |    }
                |  },
                |  "ukNonFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 678.45,
                |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
                |      "businessPremisesRenovationAllowance": 573.45,
                |      "otherCapitalAllowance": 452.34,
                |      "costOfReplacingDomesticGoods": 567.34,
                |      "electricChargePointAllowance": 454.34,
                |      "structuredBuildingAllowance": [
                |        {
                |          "amount": 234.345342,
                |          "firstYear": {
                |            "qualifyingDate": "2020-03-29",
                |            "qualifyingAmountExpenditure": 3434.453423
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
                |      "lossBroughtForward": 334.45,
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
                |""".stripMargin)
          )
        ) shouldBe List(ValueFormatError.copy(paths =
          Some(List(
            "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/amount",
            "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          ))
        ))
      }

      "fields inside enhancedStructuredBuildingAllowance are invalid" in {
        validator.validate(
          AmendUkPropertyAnnualSubmissionRawData(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse(
              """
                |{
                |  "ukFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 123.45,
                |      "businessPremisesRenovationAllowance": 345.56,
                |      "otherCapitalAllowance": 345.34,
                |      "electricChargePointAllowance": 453.34,
                |      "zeroEmissionsCarAllowance": 123.12
                |    },
                |    "adjustments": {
                |      "lossBroughtForward": 343.34,
                |      "privateUseAdjustment": 454.45,
                |      "balancingCharge": 231.45,
                |      "periodOfGraceAdjustment": true,
                |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
                |      "nonResidentLandlord": true,
                |      "rentARoom": {
                |        "jointlyLet": true
                |      }
                |    }
                |  },
                |  "ukNonFhlProperty": {
                |    "allowances": {
                |      "annualInvestmentAllowance": 678.45,
                |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
                |      "businessPremisesRenovationAllowance": 573.45,
                |      "otherCapitalAllowance": 452.34,
                |      "costOfReplacingDomesticGoods": 567.34,
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
                |          "amount": 234.4576,
                |          "firstYear": {
                |            "qualifyingDate": "2020-05-29",
                |            "qualifyingAmountExpenditure": 453.3424
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
                |      "lossBroughtForward": 334.45,
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
                |""".stripMargin)
          )
        ) shouldBe List(ValueFormatError.copy(paths =
          Some(List(
            "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/amount",
            "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          ))
        ))
      }

      "ukFhlProperty propertyIncomeAllowance is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |       "propertyIncomeAllowance": 123.455
              |          },
              |    "adjustments": {
              |      "lossBroughtForward": 343.34,
              |      "balancingCharge": 231.45,
              |      "periodOfGraceAdjustment": true,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  },
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 678.45,
              |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
              |      "businessPremisesRenovationAllowance": 573.45,
              |      "otherCapitalAllowance": 452.34,
              |      "costOfReplacingDomesticGoods": 567.34,
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
              |      "lossBroughtForward": 334.45,
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
              |""".stripMargin)
        )) shouldBe List(ValueFormatError.forPathAndRange("/ukFhlProperty/allowances/propertyIncomeAllowance", "0", "1000"))
      }

      "ukNonFhlProperty propertyIncomeAllowance is invalid" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "propertyIncomeAllowance": 345.676
              |    },
              |    "adjustments": {
              |      "lossBroughtForward": 334.45,
              |      "balancingCharge": 565.34,
              |      "businessPremisesRenovationAllowanceBalancingCharges": 563.34,
              |      "nonResidentLandlord": true,
              |      "rentARoom": {
              |        "jointlyLet": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin)
        )) shouldBe List(ValueFormatError.forPathAndRange("/ukNonFhlProperty/allowances/propertyIncomeAllowance", "0", "1000"))
      }
    }

    "for non fhl" in {
      validator.validate(AmendUkPropertyAnnualSubmissionRawData(
        validNino,
        validBusinessId,
        validTaxYear,
        Json.parse("""
            |{
            |  "ukFhlProperty": {
            |    "allowances": {
            |       "propertyIncomeAllowance": 1000.01
            |    }
            |  }
            |}
            |""".stripMargin)
      )) shouldBe List(ValueFormatError.forPathAndRange("/ukFhlProperty/allowances/propertyIncomeAllowance", "0", "1000"))
    }
  }

  "propertyIncomeAllowance allowances is too large" when {
    "for fhl" in {
      validator.validate(AmendUkPropertyAnnualSubmissionRawData(
      validNino,
      validBusinessId,
      validTaxYear,
      Json.parse("""
                 |{
                 |  "ukNonFhlProperty": {
                 |    "allowances": {
                 |      "propertyIncomeAllowance": 1000.01
                 |    }
                 |  }
                 |}
                 |""".stripMargin)
      )) shouldBe List(ValueFormatError.forPathAndRange("/ukNonFhlProperty/allowances/propertyIncomeAllowance", "0", "1000"))
    }

    "return RuleBothAllowancesSuppliedError" when {
      "allowances and propertyIncomeAllowance supplied for fhl" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          requestBodyJson
            .update("/ukFhlProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukFhlProperty/adjustments/privateUseAdjustment")
        )) shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("/ukFhlProperty/allowances"))))
      }

      "allowances and propertyIncomeAllowance supplied for non fhl" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          requestBodyJson
            .update("/ukNonFhlProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukNonFhlProperty/adjustments/privateUseAdjustment")
        )) shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances"))))
      }
    }

    "return RuleBuildingNameNumberError" when {
      "request supplied has structuredBuildingAllowance/building with no name or number" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 2000.50,
              |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
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
              |            "name": "house name",
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
      """.stripMargin)
        )) shouldBe
          List(RuleBuildingNameNumberError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building"))))
      }

      "request supplied has enhancedStructuredBuildingAllowance/building with no name or number" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "annualInvestmentAllowance": 2000.50,
              |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
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
              """.stripMargin)
        )) shouldBe
          List(RuleBuildingNameNumberError.copy(paths = Some(Seq("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building"))))
      }
    }

    "return RulePropertyIncomeAllowanceError" when {
      "propertyIncomeAllowance is supplied with privateUseAdjustment for fhl" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukFhlProperty": {
              |    "allowances": {
              |      "propertyIncomeAllowance": 1.25
              |    },
              |    "adjustments": {
              |      "privateUseAdjustment": 1.25,
              |      "nonResidentLandlord": true,
              |      "periodOfGraceAdjustment": true
              |    }
              |  }
              |}
              |""".stripMargin)
        )) shouldBe
          List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq("/ukFhlProperty"))))
      }

      "propertyIncomeAllowance is supplied with privateUseAdjustment for non-fhl" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData(
          validNino,
          validBusinessId,
          validTaxYear,
          Json.parse(
            """
              |{
              |  "ukNonFhlProperty": {
              |    "allowances": {
              |      "propertyIncomeAllowance": 1.25
              |    },
              |    "adjustments": {
              |      "privateUseAdjustment": 1.25,
              |      "nonResidentLandlord": true
              |    }
              |  }
              |}
              |""".stripMargin)
        )) shouldBe
          List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq("/ukNonFhlProperty"))))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(AmendUkPropertyAnnualSubmissionRawData("A12344A", "20178", validTaxYear, requestBodyJson)) shouldBe
          List(NinoFormatError, BusinessIdFormatError)
      }
    }
  }
}
