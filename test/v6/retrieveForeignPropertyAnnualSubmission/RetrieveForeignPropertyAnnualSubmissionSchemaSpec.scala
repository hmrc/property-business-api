/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyAnnualSubmission

import cats.data.Validated.{Invalid, Valid}
import config.MockPropertyBusinessConfig
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shared.models.domain.{TaxYear, TaxYearPropertyCheckSupport}
import shared.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionSchema.*

class RetrieveForeignPropertyAnnualSubmissionSchemaSpec
    extends UnitSpec
    with MockPropertyBusinessConfig
    with ScalaCheckDrivenPropertyChecks
    with TaxYearPropertyCheckSupport {

  "schema lookup" when {
    "a valid tax year is supplied" must {
      "use Def1 schema for tax years between 2021-22 and 2024-25" in new SetupConfig {
        forTaxYearsInRange(TaxYear.fromMtd("2021-22"), TaxYear.fromMtd("2024-25")) { taxYear =>
          schemaFor(taxYear.asMtd) shouldBe Valid(Def1)
        }
      }

      "use Def2 schema for tax year 2025-26" in new SetupConfig {
        schemaFor("2025-26") shouldBe Valid(Def2)
      }

      "use Def3 schema for tax years 2026-27 onwards" in new SetupConfig {
        forTaxYearsFrom(TaxYear.fromMtd("2026-27")) { taxYear =>
          schemaFor(taxYear.asMtd) shouldBe Valid(Def3)
        }
      }
    }

    "handle errors" when {
      "an invalid tax year is supplied" must {
        "disallow tax years prior to 2021-22 and return RuleTaxYearNotSupportedError" in new SetupConfig {
          forTaxYearsBefore(TaxYear.fromMtd("2021-22")) { taxYear =>
            schemaFor(taxYear.asMtd) shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
          }
        }
      }

      "the tax year format is invalid" must {
        "return a TaxYearFormatError" in new SetupConfig {
          schemaFor("NotATaxYear") shouldBe Invalid(Seq(TaxYearFormatError))
        }

        "the tax year range is invalid" must {
          "return a RuleTaxYearRangeInvalidError" in new SetupConfig {
            schemaFor("2020-99") shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
          }
        }
      }
    }
  }

}
