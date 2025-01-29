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

package v5.deletePropertyAnnualSubmission

import config.MockPropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v5.deletePropertyAnnualSubmission.def1.Def1_DeletePropertyAnnualSubmissionValidator
import v5.deletePropertyAnnualSubmission.model.request.DeletePropertyAnnualSubmissionRequestData

class DeletePropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"
  private val validTysTaxYear = "2023-24"

  private val validatorFactory = new DeletePropertyAnnualSubmissionValidatorFactory

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Validator[DeletePropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTysTaxYear)

        result shouldBe a[Def1_DeletePropertyAnnualSubmissionValidator]
      }

      "passed the minimum supported taxYear" in new SetupConfig {
        val result: Validator[DeletePropertyAnnualSubmissionRequestData] = validatorFactory.validator(validNino, validBusinessId, validTaxYear)

        result shouldBe a[Def1_DeletePropertyAnnualSubmissionValidator]
      }
    }

  }

}
