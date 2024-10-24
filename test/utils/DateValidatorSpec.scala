/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import api.models.errors.{FromDateFormatError, RuleToDateBeforeFromDateError, ToDateFormatError}
import cats.data.Validated
import support.UnitSpec

class DateValidatorSpec extends UnitSpec {

  private val validFromDate     = Some("2025-03-29")
  private val validToDate       = Some("2025-12-29")
  private val invalidFormatDate = Some("invalid")

  "DateValidator.validateFromAndToDates" should {
    "return valid" when {
      "given both dates in the correct format" in {
        DateValidator.validateFromAndToDates(validFromDate, validToDate) shouldBe Validated.valid(())
      }
      "given no dates" in {
        DateValidator.validateFromAndToDates(None, None) shouldBe Validated.valid(())
      }
    }
    "return an error" when {
      "the 'from' date format is incorrect" in {
        DateValidator.validateFromAndToDates(invalidFormatDate, None) shouldBe Validated.invalid(Seq(FromDateFormatError))
      }
      "the 'to' date format is incorrect" in {
        DateValidator.validateFromAndToDates(None, invalidFormatDate) shouldBe Validated.invalid(Seq(ToDateFormatError))
      }
      "the 'to' date is before the 'from' date" in {
        DateValidator.validateFromAndToDates(fromDate = validToDate, toDate = validFromDate) shouldBe Validated.invalid(
          Seq(RuleToDateBeforeFromDateError))
      }
    }
  }

}
