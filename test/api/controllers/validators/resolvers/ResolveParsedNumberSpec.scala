/*
 * Copyright 2023 HM Revenue & Customs
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

package api.controllers.validators.resolvers

import api.models.errors.ValueFormatError
import cats.data.Validated.{Invalid, Valid}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import support.UnitSpec

class ResolveParsedNumberSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks {

  private val path = "/some/path"

  "validate" when {
    "min and max are specified" must {
      val min: BigDecimal = -100
      val max: BigDecimal = 100.99

      val error   = ValueFormatError.copy(paths = Some(List(path)), message = "The value must be between -100 and 100.99")
      val resolve = ResolveParsedNumber(min, max)

      "return the error with the correct message if and only if the value is outside the inclusive range" when {
        implicit val arbitraryMoney: Arbitrary[BigDecimal] = Arbitrary(Arbitrary.arbitrary[BigInt].map(x => BigDecimal(x) / 100))

        "using validate" in forAll { money: BigDecimal =>
          resolve(money, path) shouldBe
            (if (min <= money && money <= max) Valid(money) else Invalid(List(error)))
        }

        "using validateOptional" in forAll { money: BigDecimal =>
          resolve(Some(money), path) shouldBe
            (if (min <= money && money <= max) Valid(Some(money)) else Invalid(List(error)))
        }
      }

      "more than two significant decimals are provided" when {
        "return an error for validateOptional" in {
          resolve(Some(BigDecimal(100.123)), path) shouldBe Invalid(List(error))
        }

        "return an error for validate" in {
          resolve(100.123, path) shouldBe Invalid(List(error))
        }
      }

      "no number is supplied to validateOptional" when {
        "return no error" in {
          resolve(None, path) shouldBe Valid(None)
        }
      }
    }

    "min and max are not specified" must {
      val resolve = ResolveParsedNumber()

      val error = ValueFormatError.copy(paths = Some(List(path)), message = "The value must be between 0 and 99999999999.99")

      "allow 0" in {
        resolve(0, path) shouldBe Valid(BigDecimal(0))
      }

      "disallow less than 0" in {
        resolve(-0.01, path) shouldBe Invalid(List(error))
      }

      "allow 99999999999.99" in {
        val value = BigDecimal(99999999999.99)
        resolve(value, path) shouldBe Valid(value)
      }

      "disallow more than 99999999999.99" in {
        resolve(100000000000.00, path) shouldBe Invalid(List(error))
      }
    }

    "validating when disallowing zero" when {
      val resolve = ResolveParsedNumber(min = -99999999999.99, disallowZero = true)

      "min and max are not specified" must {
        val error = ValueFormatError.copy(paths = Some(List(path)), message = "The value must be between -99999999999.99 and 99999999999.99")

        "allow -99999999999.99" in {
          val value = BigDecimal(-99999999999.99)
          resolve(value, path) shouldBe Valid(value)
        }

        "disallow less than -99999999999.99" in {
          resolve(-100000000000.00, path) shouldBe Invalid(List(error))
        }

        "allow 99999999999.99" in {
          val value = BigDecimal(99999999999.99)
          resolve(value, path) shouldBe Valid(value)
        }

        "disallow more than 99999999999.99" in {
          resolve(100000000000.00, path) shouldBe Invalid(List(error))
        }

        "not allow 0" in {
          resolve(0, path) shouldBe Invalid(List(error))
        }

        "allow None" in {
          resolve(None, path) shouldBe Valid(None)
        }
      }
    }
  }

}
