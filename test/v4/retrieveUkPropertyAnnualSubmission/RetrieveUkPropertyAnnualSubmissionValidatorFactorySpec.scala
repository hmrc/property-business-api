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

package v4.retrieveUkPropertyAnnualSubmission

import shared.controllers.validators.Validator
import config.MockAppConfig
import shared.utils.UnitSpec
import v4.retrieveUkPropertyAnnualSubmission.def1.model.Def1_RetrieveUkPropertyAnnualSubmissionValidator
import v4.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData

class RetrieveUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456B"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2022-23"
  private val validTysTaxYear = "2023-24"

  private val validatorFactory = new RetrieveUkPropertyAnnualSubmissionValidatorFactory(mockAppConfig)

  "validator" when {
    "given a valid taxYear" should {
      "return the Validator for schema definition 1" in {
        val result: Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTysTaxYear)

        result shouldBe a[Def1_RetrieveUkPropertyAnnualSubmissionValidator]
      }

      "passed the minimum supported taxYear" in {
        val result: Validator[RetrieveUkPropertyAnnualSubmissionRequestData] = validatorFactory.validator(validNino, validBusinessId, validTaxYear)

        result shouldBe a[Def1_RetrieveUkPropertyAnnualSubmissionValidator]
      }
    }

  }

}
