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

package v2.controllers.requestParsers.validators

import mocks.MockAppConfig
import play.api.libs.json.{ JsObject, JsValue, Json }
import support.UnitSpec
import v2.models.errors.{ NinoFormatError, RuleIncorrectOrEmptyBodyError, TaxYearFormatError, ValueFormatError }
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  MockAppConfig.minimumTaxHistoric returns 2017

  val validator = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator(mockAppConfig)

  val validRequestBody: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   },
     |   "annualAllowances": {
     |      "annualInvestmentAllowance": 200.00,
     |      "otherCapitalAllowance": 200.00,
     |      "businessPremisesRenovationAllowance": 100.02,
     |      "propertyIncomeAllowance": 10.02
     |   }
     | }
     |""".stripMargin)

  val validRequestBodyWithoutAnnualAllowances: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   }
     | }
     |""".stripMargin)

  val requestBodyWithInvalidAmounts: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.123,
     |      "balancingCharge": -1.00,
     |      "privateUseAdjustment": 999999999990.99,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   }
     | }
     |""".stripMargin)

  val incompleteRequestBody: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment-MISSING-BECAUSE-MISSPELT": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   }
     | }
     |""".stripMargin)

  val requestBodyWithEmptySubObjects: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |   },
     |   "annualAllowances": {
     |   }
     | }
     |""".stripMargin)

  val requestBodyWithEmptyRentARoom: JsValue = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |      }
     |   }
     | }
     |""".stripMargin)

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, validRequestBody))
        result shouldBe empty
      }

      "given a valid request that is missing the optional AnnualAllowances object" in {
        val result = validator.validate(
          CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, validRequestBodyWithoutAnnualAllowances))
        result shouldBe empty
      }
    }

    "return an Incorrect Body error" when {
      "a mandatory field is missing" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/annualAdjustments/periodOfGraceAdjustment")))
        val result   = validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, incompleteRequestBody))

        result should contain only (expected)
      }
    }

    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(
            paths =
              Some(List("/annualAdjustments/lossBroughtForward", "/annualAdjustments/privateUseAdjustment", "/annualAdjustments/balancingCharge")))

        val result =
          validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, requestBodyWithInvalidAmounts))

        result should contain only (expected)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, JsObject.empty))
        result should contain only (RuleIncorrectOrEmptyBodyError)
      }

      "given empty annualAdjustments and annualAllowances sub-objects" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(
          paths = Some(List("/annualAdjustments/periodOfGraceAdjustment", "/annualAdjustments/nonResidentLandlord")))
        val result =
          validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, requestBodyWithEmptySubObjects))

        result should contain only (expected)
      }

      "given an empty rentARoom sub-object" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/annualAdjustments/rentARoom/jointlyLet")))
        val result =
          validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, requestBodyWithEmptyRentARoom))

        result should contain only (expected)
      }
    }

    "return TaxYearFormatError error" when {
      "given an invalid taxYear" in {
        val result = validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, taxYear = "20231", validRequestBody))
        result should contain only (TaxYearFormatError)
      }
    }

    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData("BAD-NINO", validTaxYear, requestBodyWithInvalidAmounts))
        result should contain only (NinoFormatError)
      }
    }
  }
}
