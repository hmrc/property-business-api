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

package v6.retrieveUkPropertyAnnualSubmission

import api.models.domain.{TaxYear, TaxYearPropertyCheckSupport}
import api.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}
import config.MockPropertyBusinessConfig
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class RetrieveUkPropertyAnnualSubmissionSchemaSpec
    extends UnitSpec
    with ScalaCheckDrivenPropertyChecks
    with TaxYearPropertyCheckSupport
    with MockPropertyBusinessConfig {

  "schema lookup" when {
    "a tax year is present" must {

      "use Def1 for tax year 2023-24" in new SetupConfig {
        val taxYear = TaxYear.fromMtd("2023-24")
        RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(RetrieveUkPropertyAnnualSubmissionSchema.Def1)
      }

      "use Def1 for tax year 2024-25" in new SetupConfig {
        val taxYear = TaxYear.fromMtd("2024-25")
        RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(RetrieveUkPropertyAnnualSubmissionSchema.Def1)
      }

      "use Def2 for tax year 2025-26" in new SetupConfig {
        val taxYear = TaxYear.fromMtd("2025-26")
        RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(RetrieveUkPropertyAnnualSubmissionSchema.Def2)
      }

      "use Def3 for tax year 2026-27" in new SetupConfig {
        val taxYear = TaxYear.fromMtd("2026-27")
        RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(RetrieveUkPropertyAnnualSubmissionSchema.Def3)
      }
    }

    "no tax year is present (pre-TYS case)" must {
      "use Def1" in new SetupConfig {
        RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(None) shouldBe Valid(RetrieveUkPropertyAnnualSubmissionSchema.Def1)
      }
    }

    "the tax year is present but not valid" when {
      "the tax year format is invalid" must {
        "return a TaxYearFormatError" in new SetupConfig {
          RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some("NotATaxYear")) shouldBe Invalid(Seq(TaxYearFormatError))
        }
      }

      "the tax year range is invalid" must {
        "return a RuleTaxYearRangeInvalidError" in new SetupConfig {
          RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some("2020-99")) shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
        }
      }

      "the tax year range is before the minimum" must {
        "return a RuleTaxYearNotSupportedError" in new SetupConfig {
          RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some("2021-22")) shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
        }
      }
    }
  }

}
