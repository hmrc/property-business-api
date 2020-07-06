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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.ValueFormatError

class ValueValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "the minimum value is supplied" in {
        val validValue = 0
        val validationResult = ValueValidation.validateOptional(Some(validValue), "/foreignFhlEea/income/rentAmount")

        validationResult.isEmpty shouldBe true
      }
      "the maximum value is supplied" in {
        val validValue = 99999999999.99
        val validationResult = ValueValidation.validateOptional(Some(validValue), "/foreignFhlEea/income/rentAmount")

        validationResult.isEmpty shouldBe true
      }
    }
    "return a value error" when {
      "a value higher than maximum is supplied" in {
        val invalidValue = 100000000000.00
        val validationResult = ValueValidation.validateOptional(Some(invalidValue), "/foreignFhlEea/income/rentAmount")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
      }
      "a value higher than minimum is supplied" in {
        val invalidValue = -1
        val validationResult = ValueValidation.validateOptional(Some(invalidValue), "/foreignFhlEea/income/rentAmount")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
      }
      "a value with more than 2 decimal points is supplied" in {
        val invalidValue = 5009.3921
        val validationResult = ValueValidation.validateOptional(Some(invalidValue), "/foreignFhlEea/income/rentAmount")

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq("/foreignFhlEea/income/rentAmount")))
      }
    }
  }
}
