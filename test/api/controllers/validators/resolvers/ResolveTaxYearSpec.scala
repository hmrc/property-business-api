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
import api.models.errors.{InvalidTaxYearParameterError, RuleHistoricTaxYearNotSupportedError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalid, TaxYearFormatError}
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveTaxYearSpec extends UnitSpec {

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
  }

  "ResolveTaxYear with a minimum taxYear" should {
    val minimumTaxYear = TaxYear.fromMtd("2021-22")

    "return no errors" when {
      "the historic tax year supplied is the minimum allowed" in {
        ResolveTaxYear(minimumTaxYear, "2021-22", None, None) shouldBe
          Valid(TaxYear.fromMtd("2021-22"))
      }
    }

    "return RuleHistoricTaxYearNotSupportedError" when {
      "when the tax year is before the minimum tax year" in {
        ResolveTaxYear(minimumTaxYear, "2020-21", None, None) shouldBe
          Invalid(List(RuleTaxYearNotSupportedError))
      }
    }
  }

  "ResolveTysTaxYear" should {
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
  }

  "ResolveHistoricTaxYear" should {
    val minimumHistoricTaxYear = TaxYear.fromMtd("2021-22")
    val maximumHistoricTaxYear = TaxYear.fromMtd("2025-26")

    "return no errors" when {
      "the historic tax year supplied is the minimum allowed" in allow("2021-22")
      "the historic tax year supplied is the maximum allowed" in allow("2025-26")

      def allow(taxYearString: String): Unit =
        ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, taxYearString) shouldBe
          Valid(TaxYear.fromMtd(taxYearString))
    }

    "return RuleHistoricTaxYearNotSupportedError" when {
      "when the tax year is before the minimum tax year" in disallow("2020-21")
      "when the tax year is after the maximum tax year" in disallow("2026-27")

      def disallow(taxYearString: String): Unit =
        ResolveHistoricTaxYear(minimumHistoricTaxYear, maximumHistoricTaxYear, taxYearString) shouldBe
          Invalid(List(RuleHistoricTaxYearNotSupportedError))
    }
  }

}
