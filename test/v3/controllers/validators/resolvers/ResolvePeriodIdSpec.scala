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

package v3.controllers.validators.resolvers

import api.models.domain.PeriodId
import api.models.errors.PeriodIdFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolvePeriodIdSpec extends UnitSpec {

  private val minTaxYear = 2017
  private val maxTaxYear = 2021

  "ResolvePeriodIdSpec" should {
    "return no errors" when {
      "passed a valid PeriodId" in {
        val periodId = "2019-04-06_2019-07-04"
        val result   = ResolvePeriodId(minTaxYear, maxTaxYear, periodId, None, None)
        result shouldBe Valid(PeriodId(periodId))
      }
      "passed a periodId equal to the minimum" in {
        val minPeriodId = "2017-04-06_2017-07-04"
        val result      = ResolvePeriodId(minTaxYear, maxTaxYear, minPeriodId, None, None)
        result shouldBe Valid(PeriodId(minPeriodId))
      }

      "passed a periodId equal to the maximum" in {
        val maxPeriodId = "2021-04-06_2021-07-04"
        val result      = ResolvePeriodId(minTaxYear, maxTaxYear, maxPeriodId, None, None)
        result shouldBe Valid(PeriodId(maxPeriodId))
      }

    }

    "return an error" when {
      "passed a PeriodId with an invalid format" in {
        val invalidPeriodId = "2017-04-06__2017-07-04"
        val result          = ResolvePeriodId(minTaxYear, maxTaxYear, invalidPeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId with an invalid start date format" in {
        val invalidPeriodId = "20A7-04-06_2017-07-04"
        val result          = ResolvePeriodId(minTaxYear, maxTaxYear, invalidPeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId with an invalid end date format" in {
        val invalidPeriodId = "2017-04-06_20A7-07-04"
        val result          = ResolvePeriodId(minTaxYear, maxTaxYear, invalidPeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId with a invalid underscore" in {
        val invalidPeriodId = "20A7-04-06a2017-07-04"
        val result          = ResolvePeriodId(minTaxYear, maxTaxYear, invalidPeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId before the minimum" in {
        val earlyPeriodId = "2016-04-06_2016-07-04"
        val result        = ResolvePeriodId(minTaxYear, maxTaxYear, earlyPeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId after the maximum" in {
        val latePeriodId = "2022-04-06_2022-07-04"
        val result       = ResolvePeriodId(minTaxYear, maxTaxYear, latePeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }

      "passed a PeriodId with toDate before fromDate" in {
        val latePeriodId = "2022-07-04_2022-04-06"
        val result       = ResolvePeriodId(minTaxYear, maxTaxYear, latePeriodId, None, None)
        result shouldBe Invalid(List(PeriodIdFormatError))
      }
    }
  }

}
