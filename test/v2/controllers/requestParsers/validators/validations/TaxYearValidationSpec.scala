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

package v2.controllers.requestParsers.validators.validations

import support.UnitSpec
import v2.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}

class TaxYearValidationSpec extends UnitSpec {

  val taxYear = "2021-22"

  "validate" should {
    "return no errors" when {
      "a valid taxYear is supplied" in {
        val validationResult = TaxYearValidation.validate(taxYear)

        validationResult.isEmpty shouldBe true
      }
    }
    "return an error" when {
      "a taxYear with an invalid format is supplied" in {
        val validationResult = TaxYearValidation.validate("2019/20")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe TaxYearFormatError
      }
      "a taxYear with a range longer than 1 is supplied" in {
        val validationResult = TaxYearValidation.validate("2021-23")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearRangeInvalidError
      }
      "a taxYear that isn't the minimum is supplied" in {
        val validationResult = TaxYearValidation.validate("2020-21")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearNotSupportedError
      }
    }
  }

}
