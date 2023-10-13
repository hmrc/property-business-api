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

import api.models.domain.TaxYear
import api.models.errors.{InvalidTaxYearParameterError, RuleHistoricTaxYearNotSupportedError, RuleTaxYearRangeInvalid, TaxYearFormatError}
import cats.data.Validated.{Invalid, Valid}
import api.support.UnitSpec

class ResolveTaxYearSpec extends UnitSpec {

  private val minimumHistoricTaxYear = 2021
  private val maximumHistoricTaxYear = 2025

  "ResolveTaxYear" should {
    "return no errors" when {
      "passed a valid tax year" in {
        val validTaxYear = "2018-19"
        val result       = ResolveTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed an invalid tax year format" in {
        val result = ResolveTaxYear("2019")
        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "passed a tax year string in which the range is greater than 1 year" in {
        val result = ResolveTaxYear("2017-19")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the end year is before the start year" in {
        val result = ResolveTaxYear("2018-17")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the start and end years are the same" in {
        val result = ResolveTaxYear("2017-17")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the tax year is bad" in {
        val result = ResolveTaxYear("20177-17")
        result shouldBe Invalid(List(TaxYearFormatError))
      }
    }

    "return no errors" when {
      "passed a valid tax year that's above or equal to TaxYear.tysTaxYear" in {
        val validTaxYear = "2023-24"
        val result       = ResolveTysTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed a valid tax year but below TaxYear.tysTaxYear" in {
        val result = ResolveTysTaxYear("2021-22")
        result shouldBe Invalid(List(InvalidTaxYearParameterError))
      }
    }

    "return no errors" when {
      "the historic tax year supplied is the minimum allowed" in {
        val validTaxYear = "2021-22"
        val result       = ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, validTaxYear, None, None)

        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }

      "the historic tax year supplied is the maximum allowed" in {
        val validTaxYear = "2025-26"
        val result       = ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, validTaxYear, None, None)

        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return RuleHistoricTaxYearNotSupportedError" when {
      "when the tax year is before the minimum tax year" in {
        val invalidTaxYear = "2020-21"

        val result = ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, invalidTaxYear, None, None)

        result shouldBe Invalid(List(RuleHistoricTaxYearNotSupportedError))
      }

      "when the tax year is before the minim the maximum tax year" in {
        val invalidTaxYear = "2026-27"

        val result = ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, invalidTaxYear, None, None)

        result shouldBe Invalid(List(RuleHistoricTaxYearNotSupportedError))
      }
    }
  }

}
