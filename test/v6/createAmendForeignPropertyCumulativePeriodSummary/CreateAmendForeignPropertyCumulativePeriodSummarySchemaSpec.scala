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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import cats.data.Validated.{Invalid, Valid}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shared.models.domain.TaxYear
import shared.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import shared.utils.UnitSpec

class CreateAmendForeignPropertyCumulativePeriodSummarySchemaSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks {

  "schema lookup" when {
    "a tax year is present" should {
      "use Def1 for tax year 2025-26" in {
        val taxYear = TaxYear.fromMtd("2025-26")
        CreateAmendForeignPropertyCumulativePeriodSummarySchema.schemaFor(taxYear.asMtd) shouldBe Valid(
          CreateAmendForeignPropertyCumulativePeriodSummarySchema.Def1)
      }
    }

    "an invalid tax year is present" should {
      "return a RuleTaxYearNotSupportedError" in {
        CreateAmendForeignPropertyCumulativePeriodSummarySchema.schemaFor("2024-25") shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
      }
      "return a TaxYearFormatError" in {
        CreateAmendForeignPropertyCumulativePeriodSummarySchema.schemaFor("NotATaxYear") shouldBe Invalid(Seq(TaxYearFormatError))
      }
      "return a RuleTaxYearRangeInvalidError" in {
        CreateAmendForeignPropertyCumulativePeriodSummarySchema.schemaFor("2020-99") shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
      }
    }
  }

}
