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

package v6.createAmendUkPropertyAnnualSubmission

import config.MockPropertyBusinessConfig
import play.api.libs.json._
import shared.controllers.validators.AlwaysErrorsValidator
import shared.utils.UnitSpec
import v6.createAmendUkPropertyAnnualSubmission.def1.Def1_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.def2.Def2_CreateAmendUkPropertyAnnualSubmissionValidator

class CreateAmendUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private def validatorFor(taxYear: String) = {
    val validatorFactory = new CreateAmendUkPropertyAnnualSubmissionValidatorFactory
    validatorFactory.validator(nino = "ignoredNino", businessId = "ignored", taxYear = taxYear, body = JsObject.empty)
  }

  "CreateAmendUkPropertyAnnualSubmissionValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in new SetupConfig {
        validatorFor("2024-25") shouldBe a[Def1_CreateAmendUkPropertyAnnualSubmissionValidator]
      }
    }

    "given a request corresponding to a Def2 schema" should {
      "return a Def2 validator" in new SetupConfig {
        validatorFor("2025-26") shouldBe a[Def2_CreateAmendUkPropertyAnnualSubmissionValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in new SetupConfig {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
