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

import api.models.errors.MtdError
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxOption
import support.UnitSpec

class ResolversSpec extends UnitSpec with Resolvers {
  private val notInteger = MtdError("NOT_INT", "Not integer", 400)
  private val outOfRange = MtdError("OUT_OF_RANGE", "Out of range", 400)
  private val odd        = MtdError("ODD", "Odd", 400)

  private val resolveInt: SimpleResolver[String, Int] = _.toIntOption.toValid(List(notInteger))

  "Resovlers" must {
    "provide the ability to easily create predicate based validators" in {
      val validator = satisfies[Int](outOfRange)(_ < 10)

      validator(1) shouldBe None
      validator(10) shouldBe Some(List(outOfRange))
    }

    "provide the ability to easily create minimum allowed based validators" in {
      val validator = satisfiesMin[Int](10, outOfRange)

      validator(9) shouldBe Some(List(outOfRange))
      validator(10) shouldBe None
      validator(11) shouldBe None
    }

    "provide the ability to easily create maximum allowed based validators" in {
      val validator = satisfiesMax[Int](10, outOfRange)

      validator(9) shouldBe None
      validator(10) shouldBe None
      validator(11) shouldBe Some(List(outOfRange))
    }

    "provide the ability to easily create validators based on a min max range" in {
      val validator = inRange[Int](8, 10, outOfRange)

      validator(7) shouldBe Some(List(outOfRange))
      validator(9) shouldBe None
      validator(10) shouldBe None
      validator(11) shouldBe Some(List(outOfRange))
    }

    "provide the ability to compose a resolver with a subsequent validator" in {
      val resolver = resolveInt thenValidate satisfiesMax(9, outOfRange)

      resolver("9") shouldBe Valid(9)
      resolver("10") shouldBe Invalid(List(outOfRange))
      resolver("xx") shouldBe Invalid(List(notInteger))
    }

    "provide the ability to compose a resolver with multiple subsequent validators (and keep all errors)" in {
      val isEven = satisfies[Int](odd)(_ % 2 == 0)

      val resolver = resolveInt thenValidate combinedValidator(satisfiesMax(10, outOfRange), isEven)

      resolver("2") shouldBe Valid(2)
      resolver("3") shouldBe Invalid(List(odd))
      resolver("10") shouldBe Valid(10)
      resolver("11") shouldBe Invalid(List(outOfRange, odd))
      resolver("12") shouldBe Invalid(List(outOfRange))
      resolver("xx") shouldBe Invalid(List(notInteger))
    }

    "provide the ability to map a valid result" in {
      val resolver = resolveInt map (v => -v)

      resolver("2") shouldBe Valid(-2)
      resolver("xx") shouldBe Invalid(List(notInteger))
    }

    "provide the ability to validate against an optional value" in {
      val resolver = resolveInt.resolveOptionally

      resolver(Some("1")) shouldBe Valid(Some(1))
      resolver(Some("xx")) shouldBe Invalid(List(notInteger))
      resolver(None) shouldBe Valid(None)
    }

    "provide the ability to validate against an optional value with a default" in {
      val resolver = resolveInt.resolveOptionallyWithDefault(0)

      resolver(Some("1")) shouldBe Valid(1)
      resolver(Some("xx")) shouldBe Invalid(List(notInteger))
      resolver(None) shouldBe Valid(0)
    }
  }

}
