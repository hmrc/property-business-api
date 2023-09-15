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

package v2.controllers.validators.resolvers

import api.models.domain.DateRange
import api.models.errors.{FromDateOutOfRangeError, ToDateOutOfRangeError}
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

import java.time.LocalDate

class ResolveFromAndToDatesSpec extends UnitSpec {

  private val minTaxYear = 2017
  private val maxTaxYear = 2021

  val resolveFromAndToDates = new ResolveFromAndToDates(minTaxYear, maxTaxYear)

  "ResolvePeriodIdSpec" should {
    "return no errors" when {
      "passed valid from and to dates" in {
        val fromDate = "2019-04-06"
        val toDate   = "2019-08-06"

        val result = resolveFromAndToDates((fromDate, toDate), None, None)

        result shouldBe Valid(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate)))
      }

      "passed valid from and to dates equal to the minimum and maximum" in {
        val fromDate = "2018-04-06"
        val toDate   = "2020-04-06"

        val result = resolveFromAndToDates((fromDate, toDate), None, None)

        result shouldBe Valid(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate)))
      }
    }

    "return an error" when {
      "passed a fromYear less than or equal to minimumTaxYear" in {
        val result = resolveFromAndToDates(("2017-04-06", "2019-04-05"), None, None)
        result shouldBe Invalid(List(FromDateOutOfRangeError))
      }

      "passed a toYear greater than or equal to maximumTaxYear" in {
        val result = resolveFromAndToDates(("2020-04-06", "2021-04-05"), None, None)
        result shouldBe Invalid(List(ToDateOutOfRangeError))
      }

      "passed both dates that are out of range" in {
        val result = resolveFromAndToDates(("2017-04-06", "2021-04-05"), None, None)
        result shouldBe Invalid(List(FromDateOutOfRangeError, ToDateOutOfRangeError))
      }

    }
  }

}
