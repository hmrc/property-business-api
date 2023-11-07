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

import api.models.domain.{TaxYear, TodaySupplier}
import api.models.errors._
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveTaxYearSpec extends UnitSpec with ResolverSupport {

  "ResolveTaxYear" should {
    "return no errors" when {
      "passed a valid tax year" in {
        val validTaxYear = "2018-19"
        ResolveTaxYear(validTaxYear) shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed an invalid tax year format" in {
        ResolveTaxYear("2019") shouldBe Invalid(List(TaxYearFormatError))
      }

      "passed a tax year string in which the range is greater than 1 year" in {
        ResolveTaxYear("2017-19") shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the end year is before the start year" in {
        ResolveTaxYear("2018-17") shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the start and end years are the same" in {
        ResolveTaxYear("2017-17") shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the tax year is bad" in {
        ResolveTaxYear("20177-17") shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

  "ResolveTaxYearMinimum" should {
    val minimumTaxYear = TaxYear.fromMtd("2021-22")
    val resolver       = ResolveTaxYearMinimum(minimumTaxYear)

    "return no errors" when {
      "the tax year supplied is the minimum allowed" in {
        resolver("2021-22") shouldBe Valid(minimumTaxYear)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "when the tax year is before the minimum tax year" in {
        resolver("2020-21") shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }
  }

  "ResolveTysTaxYear" should {
    "return no errors" when {
      "passed a valid tax year that's above or equal to TaxYear.tysTaxYear" in {
        val validTaxYear = "2023-24"
        ResolveTysTaxYear(validTaxYear) shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed a valid tax year but below TaxYear.tysTaxYear" in {
        ResolveTysTaxYear("2021-22") shouldBe Invalid(List(InvalidTaxYearParameterError))
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

  "validateIncompleteTaxYear" should {
    val error = MtdError("SOME_ERROR", "Message", 400)
    def resolver(implicit todaySupplier: TodaySupplier): Resolver[String, TaxYear] = ResolveIncompleteTaxYear(error).resolver

    val taxYearString = "2020-21"
    val taxYear       = TaxYear.fromMtd(taxYearString)

    "accept when now is after the tax year ends" in {
      val todaySupplier = TodaySupplier.fixed(taxYear.endDate.plusDays(1))
      resolver(todaySupplier)(taxYearString) shouldBe Valid(taxYear)
    }

    "reject when now is on the day the tax year ends" in {
      val todaySupplier = TodaySupplier.fixed(taxYear.endDate)
      resolver(todaySupplier)(taxYearString) shouldBe Invalid(List(error))
    }

    "reject when now is before the tax year starts" in {
      val todaySupplier = TodaySupplier.fixed(taxYear.startDate.minusDays(1))
      resolver(todaySupplier)(taxYearString) shouldBe Invalid(List(error))
    }

  }

}
