/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionRawData

class AmendForeignPropertyAnnualSubmissionValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
      |{
      |   "foreignFhlEea":
      |      {
      |         "adjustments":{
      |            "privateUseAdjustment":100.25,
      |            "balancingCharge":100.25,
      |            "periodOfGraceAdjustment":true
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      },
      |   "foreignProperty":[
      |      {
      |         "countryCode":"FRA",
      |         "adjustments":
      |            {
      |               "privateUseAdjustment":100.25,
      |               "balancingCharge":100.25
      |            }
      |         ,
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "costOfReplacingDomesticItems":100.25,
      |            "zeroEmissionsGoodsVehicleAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "structureAndBuildingAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  val validator = new AmendForeignPropertyAnnualSubmissionValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a minimal foreignProperty request is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA"
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe Nil
      }
      "a foreignProperty with multiple objects is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe Nil
      }
      "only a foreignProperty is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe Nil
      }
      "only a foreignEeaFhl is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":{
            |      "adjustments":{
            |         "privateUseAdjustment":100.25,
            |         "balancingCharge":100.25,
            |         "periodOfGraceAdjustment":true
            |      },
            |      "allowances":{
            |         "annualInvestmentAllowance":100.25,
            |         "otherCapitalAllowance":100.25,
            |         "propertyAllowance":100.25,
            |         "electricChargePointAllowance":100.25
            |      }
            |   }
            |}
            |""".stripMargin
        ))) shouldBe Nil
      }
    }
    "return a path parameter error" when {
      "an invalid nino is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData("A12344A", validBusinessId, validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
      "an invalid businessId is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, "Walrus", validTaxYear, requestBodyJson)) shouldBe List(BusinessIdFormatError)
      }
      "an invalid taxYear is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2103/01", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
      "an invalid taxYear range is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2022-24", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "a below minimum taxYear is supplied" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, "2018-19", requestBodyJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }
    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty body is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse("""{}"""))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignFhlEea is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |  "foreignFhlEea": {}
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty foreignProperty is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |   "foreignProperty": [
            |   {}
            |   ]
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty /foreignFhlEea/allowances is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |   "foreignFhlEea": {
            |     "allowances": {}
            |   }
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty /foreignFhlEea/adjustments is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |   "foreignFhlEea": {
            |     "adjustments": {}
            |   }
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty /foreignProperty/allowances is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |   "foreignProperty": [
            |     {
            |     "allowances": {}
            |     }
            |   ]
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty /foreignProperty/adjustments is submitted" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """{
            |   "foreignProperty": [
            |     {
            |     "adjustments": {}
            |     }
            |   ]
            |}""".stripMargin))) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }
    "return ValueFormatError" when {
      "/foreignFhlEea/adjustments/privateUseAdjustment is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment": -1,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/adjustments/privateUseAdjustment"))))
      }
      "/foreignFhlEea/adjustments/balancingCharge is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":-1,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/adjustments/balancingCharge"))))
      }
      "/foreignFhlEea/allowances/annualInvestmentAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":-1,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/allowances/annualInvestmentAllowance"))))
      }
      "/foreignFhlEea/allowances/otherCapitalAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":-1,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/allowances/otherCapitalAllowance"))))
      }
      "/foreignFhlEea/allowances/propertyAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":-1,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/allowances/propertyAllowance"))))
      }
      "/foreignFhlEea/allowances/electricChargePointAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":-1
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/allowances/electricChargePointAllowance"))))
      }
      "/foreignProperty/adjustments/privateUseAdjustment is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":-1,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/adjustments/privateUseAdjustment"))))
      }
      "/foreignProperty/adjustments/balancingCharge is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":-1
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/adjustments/balancingCharge"))))
      }
      "/foreignProperty/allowances/annualInvestmentAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":-1,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/annualInvestmentAllowance"))))
      }
      "/foreignProperty/allowances/costOfReplacingDomesticItems is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":-1,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/costOfReplacingDomesticItems"))))
      }
      "/foreignProperty/allowances/zeroEmissionsGoodsVehicleAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":-1,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance"))))
      }
      "/foreignProperty/allowances/propertyAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":-1,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/propertyAllowance"))))
      }
      "/foreignProperty/allowances/otherCapitalAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":-1,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/otherCapitalAllowance"))))
      }
      "/foreignProperty/allowances/structureAndBuildingAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":-1,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/structureAndBuildingAllowance"))))
      }
      "/foreignProperty/allowances/electricChargePointAllowance is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"FRA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":-1
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(ValueFormatError.copy(paths = Some(Seq("/foreignProperty/0/allowances/electricChargePointAllowance"))))
      }
    }
    "return a country code error" when {
      "countryCode format is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"AJSA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("/foreignProperty/0/countryCode"))))
      }
      "the three character code isn't ISO 3166-1 alpha-3" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":100.25,
            |            "balancingCharge":100.25,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"GER",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":100.25,
            |               "balancingCharge":100.25
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":100.25,
            |            "costOfReplacingDomesticItems":100.25,
            |            "zeroEmissionsGoodsVehicleAllowance":100.25,
            |            "propertyAllowance":100.25,
            |            "otherCapitalAllowance":100.25,
            |            "structureAndBuildingAllowance":100.25,
            |            "electricChargePointAllowance":100.25
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("/foreignProperty/0/countryCode"))))
      }
    }
    "return multiple errors" when {
      "every path parameter format is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData("AJAA12", "XASOE12", "201219", requestBodyJson)) shouldBe List(NinoFormatError, BusinessIdFormatError, TaxYearFormatError)
      }
      "every field in the body is invalid" in {
        validator.validate(AmendForeignPropertyAnnualSubmissionRawData(validNino, validBusinessId, validTaxYear, Json.parse(
          """
            |{
            |   "foreignFhlEea":
            |      {
            |         "adjustments":{
            |            "privateUseAdjustment":-1,
            |            "balancingCharge":-1,
            |            "periodOfGraceAdjustment":true
            |         },
            |         "allowances":{
            |            "annualInvestmentAllowance":-1,
            |            "otherCapitalAllowance":-1,
            |            "propertyAllowance":-1,
            |            "electricChargePointAllowance":-1
            |         }
            |      },
            |   "foreignProperty":[
            |      {
            |         "countryCode":"AJDnA",
            |         "adjustments":
            |            {
            |               "privateUseAdjustment":-1,
            |               "balancingCharge":-1
            |            }
            |         ,
            |         "allowances":{
            |            "annualInvestmentAllowance":-1,
            |            "costOfReplacingDomesticItems":-1,
            |            "zeroEmissionsGoodsVehicleAllowance":-1,
            |            "propertyAllowance":-1,
            |            "otherCapitalAllowance":-1,
            |            "structureAndBuildingAllowance":-1,
            |            "electricChargePointAllowance":-1
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        ))) shouldBe List(
          CountryCodeFormatError.copy(paths = Some(Seq(
            "/foreignProperty/0/countryCode"
          ))),
          ValueFormatError.copy(paths = Some(Seq(
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
          "/foreignProperty/0/allowances/electricChargePointAllowance"))))
      }
    }
  }
}
