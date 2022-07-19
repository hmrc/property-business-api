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

package v2.controllers.requestParsers.validators.validations

import support.UnitSpec
import v2.models.errors.{RuleHistoricTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}

class HistoricTaxYearValidationSpec extends UnitSpec {

  val minTaxYear = 2017
  val maxTaxYear = 2021
  val taxYear    = "2021-22"


  "validate" should {
    "return no errors" when {
      "a valid TaxYear is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, taxYear)

        validationResult shouldBe Nil
      }
      "the minimum TaxYear is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, "2017-18")

        validationResult shouldBe Nil
      }
    }
    "return an error" when {
      "a taxYear with an invalid format is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, "2019/20")

        validationResult shouldBe List(TaxYearFormatError)
      }
      "a taxYear with a range longer than 1 is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, "2021-23")

        validationResult shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "a taxYear before the minimum is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, "2016-17")

        validationResult shouldBe List(RuleHistoricTaxYearNotSupportedError)
      }
      "a taxYear after the maximum is supplied" in {
        val validationResult = HistoricTaxYearValidation.validate(minTaxYear, maxTaxYear, "2022-23")

        validationResult shouldBe List(RuleHistoricTaxYearNotSupportedError)
      }
    }
  }
}
