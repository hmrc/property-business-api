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

import api.models.errors.{FromDateFormatError, RuleMissingSubmissionDatesError, RuleToDateBeforeFromDateError, ToDateFormatError}
import cats.data.Validated.{invalid, valid}
import support.UnitSpec

class DateValidatorSpec extends UnitSpec {

  private val validFromDate     = Some("2025-03-29")
  private val validToDate       = Some("2025-12-29")
  private val invalidFormatDate = Some("invalid")

  "DateValidator.validateFromAndToDates" should {
    "return valid" when {
      "both dates are supplied in the correct format" in {
        DateValidator.validateFromAndToDates(validFromDate, validToDate) shouldBe valid(())
      }

      "no dates are supplied" in {
        DateValidator.validateFromAndToDates(None, None) shouldBe valid(())
      }
    }

    "return error(s)" when {
      "a single date is provided" which {
        "is a fromDate with an invalid format" in {
          DateValidator.validateFromAndToDates(invalidFormatDate, None) shouldBe invalid(Seq(RuleMissingSubmissionDatesError))
        }
        "is a toDate with an invalid format" in {
          DateValidator.validateFromAndToDates(None, invalidFormatDate) shouldBe invalid(Seq(RuleMissingSubmissionDatesError))
        }
        "is a fromDate with a valid format" in {
          DateValidator.validateFromAndToDates(validFromDate, None) shouldBe invalid(Seq(RuleMissingSubmissionDatesError))
        }
        "is a toDate with a valid format" in {
          DateValidator.validateFromAndToDates(None, validToDate) shouldBe invalid(Seq(RuleMissingSubmissionDatesError))
        }
      }

      "both fromDate and toDate are provided" which {
        "include an invalid fromDate format" in {
          DateValidator.validateFromAndToDates(invalidFormatDate, validToDate) shouldBe invalid(Seq(FromDateFormatError))
        }
        "include an invalid toDate format" in {
          DateValidator.validateFromAndToDates(validFromDate, invalidFormatDate) shouldBe invalid(Seq(ToDateFormatError))
        }
        "have invalid formats for both dates" in {
          DateValidator.validateFromAndToDates(invalidFormatDate, invalidFormatDate) shouldBe invalid(
            Seq(FromDateFormatError, ToDateFormatError)
          )
        }
        "has toDate earlier than the fromDate" in {
          DateValidator.validateFromAndToDates(validToDate, validFromDate) shouldBe invalid(Seq(RuleToDateBeforeFromDateError))
        }
      }
    }
  }

}
