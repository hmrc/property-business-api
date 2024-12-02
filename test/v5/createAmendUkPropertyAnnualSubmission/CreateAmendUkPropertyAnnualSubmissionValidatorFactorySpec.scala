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

package v5.createAmendUkPropertyAnnualSubmission

import api.controllers.validators.AlwaysErrorsValidator
import config.MockAppConfig
import mocks.MockJsonReadsWrites
import play.api.libs.json._
import support.UnitSpec
import v5.createAmendUkPropertyAnnualSubmission.def1.Def1_CreateAmendUkPropertyAnnualSubmissionValidator
import v5.createAmendUkPropertyAnnualSubmission.def2.Def2_CreateAmendUkPropertyAnnualSubmissionValidator

class CreateAmendUkPropertyAnnualSubmissionValidatorFactorySpec(implicit costOfReplacingDomesticItemsKey: String)
  extends UnitSpec with MockAppConfig with MockJsonReadsWrites {

  private def validatorFor(taxYear: String) =
    new CreateAmendUkPropertyAnnualSubmissionValidatorFactory(mockAppConfig).validator(
      nino = "ignoredNino",
      businessId = "ignored",
      taxYear = taxYear,
      body = JsObject.empty)

  "CreateAmendUkPropertyAnnualSubmissionValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in {
        validatorFor("2024-25") shouldBe a[Def1_CreateAmendUkPropertyAnnualSubmissionValidator]
      }
    }

    "given a request corresponding to a Def2 schema" should {
      "return a Def2 validator" in {
        validatorFor("2025-26") shouldBe a[Def2_CreateAmendUkPropertyAnnualSubmissionValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
