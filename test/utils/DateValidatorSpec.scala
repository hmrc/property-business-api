
package utils

import api.models.errors.{FromDateFormatError, RuleToDateBeforeFromDateError, ToDateFormatError}
import cats.data.Validated
import support.UnitSpec

class DateValidatorSpec extends UnitSpec {
  
  private val validFromDate = Some("2025-03-29")
  private val validToDate = Some("2025-12-29")
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
      "when the 'from' date format is incorrect" in {
        DateValidator.validateFromAndToDates(invalidFormatDate, None) shouldBe Validated.invalid(Seq(FromDateFormatError))
      }
      "when the 'to' date format is incorrect" in {
        DateValidator.validateFromAndToDates(None, invalidFormatDate) shouldBe Validated.invalid(Seq(ToDateFormatError))
      }
      "when the 'to' date is before the 'from' date" in {
        DateValidator.validateFromAndToDates(fromDate = validToDate, toDate = validFromDate) shouldBe Validated.invalid(Seq(RuleToDateBeforeFromDateError))
      }
    }
  }
}
