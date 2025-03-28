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

package v5.retrieveForeignPropertyAnnualSubmission

import cats.data.Validated.{Invalid, Valid}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shared.models.domain.TaxYear
import shared.models.errors.{RuleTaxYearRangeInvalidError, TaxYearFormatError}
import shared.utils.UnitSpec

class RetrieveForeignPropertyAnnualSubmissionSchemaSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks {

  "schema lookup" when {
    "a tax year is present" must {

      "use Def1 for tax year 2023-24" in {
        val taxYear = TaxYear.fromMtd("2023-24")
        RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(
          RetrieveForeignPropertyAnnualSubmissionSchema.Def1)
      }

      "use Def1 for tax year 2024-25" in {
        val taxYear = TaxYear.fromMtd("2024-25")
        RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(
          RetrieveForeignPropertyAnnualSubmissionSchema.Def1)
      }

      "use Def2 for tax years 2025-26 onwards" in {
        val taxYear = TaxYear.fromMtd("2025-26")
        RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear.asMtd)) shouldBe Valid(
          RetrieveForeignPropertyAnnualSubmissionSchema.Def2)
      }
    }

    "no tax year is present (pre-TYS case)" must {
      "use Def1" in {
        RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(None) shouldBe Valid(RetrieveForeignPropertyAnnualSubmissionSchema.Def1)
      }
    }

    "the tax year is present but not valid" when {
      "the tax year format is invalid" must {
        "return a TaxYearFormatError" in {
          RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some("NotATaxYear")) shouldBe Invalid(Seq(TaxYearFormatError))
        }
      }

      "the tax year range is invalid" must {
        "return a RuleTaxYearRangeInvalidError" in {
          RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some("2020-99")) shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
        }
      }
    }
  }

}
