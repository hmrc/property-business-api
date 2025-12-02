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

import config.MockPropertyBusinessConfig
import shared.controllers.validators.{AlwaysErrorsValidator, Validator}
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyAnnualSubmission.def1.Def1_RetrieveForeignPropertyAnnualSubmissionValidator
import v6.retrieveForeignPropertyAnnualSubmission.def2.Def2_RetrieveForeignPropertyAnnualSubmissionValidator
import v6.retrieveForeignPropertyAnnualSubmission.def3.Def3_RetrieveForeignPropertyAnnualSubmissionValidator
import v6.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

class RetrieveForeignPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private def validatorFor(taxYear: String): Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] =
    new RetrieveForeignPropertyAnnualSubmissionValidatorFactory().validator(
      nino = "ignoredNino",
      businessId = "ignoredBusinessId",
      taxYear = taxYear,
      propertyId = None
    )

  "RetrieveForeignPropertyAnnualSubmissionValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in new SetupConfig {
        validatorFor("2024-25") shouldBe a[Def1_RetrieveForeignPropertyAnnualSubmissionValidator]
      }
    }

    "given a request corresponding to a Def2 schema" should {
      "return a Def2 validator" in new SetupConfig {
        validatorFor("2025-26") shouldBe a[Def2_RetrieveForeignPropertyAnnualSubmissionValidator]
      }
    }

    "given a request corresponding to a Def3 schema" should {
      "return a Def3 validator" in new SetupConfig {
        validatorFor("2026-27") shouldBe a[Def3_RetrieveForeignPropertyAnnualSubmissionValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in new SetupConfig {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
