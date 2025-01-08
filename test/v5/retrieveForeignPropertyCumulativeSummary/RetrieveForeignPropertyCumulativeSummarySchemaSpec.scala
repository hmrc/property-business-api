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

package v5.retrieveForeignPropertyCumulativeSummary

import cats.data.Validated.{Invalid, Valid}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shared.models.domain.{TaxYear, TaxYearPropertyCheckSupport}
import shared.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import shared.utils.UnitSpec

class RetrieveForeignPropertyCumulativeSummarySchemaSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks with TaxYearPropertyCheckSupport {

  "schema lookup" when {
    "a correctly formatted tax year is supplied" must {
      "disallow tax years prior to 2025-26 and return RuleTaxYearNotSupportedError" in {
        forTaxYearsBefore(TaxYear.fromMtd("2025-26")) { taxYear =>
          RetrieveForeignPropertyCumulativeSummarySchema.schemaFor(taxYear.asMtd) shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
        }
      }

      "use Def1 for tax years 2025-26 onwards" in {
        forTaxYearsFrom(TaxYear.fromMtd("2025-26")) { taxYear =>
          RetrieveForeignPropertyCumulativeSummarySchema.schemaFor(taxYear.asMtd) shouldBe Valid(RetrieveForeignPropertyCumulativeSummarySchema.Def1)
        }
      }
    }

    "a badly formatted tax year is supplied" must {
      "the tax year format is invalid" must {
        "return a TaxYearFormatError" in {
          RetrieveForeignPropertyCumulativeSummarySchema.schemaFor("NotATaxYear") shouldBe Invalid(Seq(TaxYearFormatError))
        }
      }

      "the tax year range is invalid" must {
        "return a RuleTaxYearRangeInvalidError" in {
          RetrieveForeignPropertyCumulativeSummarySchema.schemaFor("2020-99") shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
        }
      }
    }
  }

}
