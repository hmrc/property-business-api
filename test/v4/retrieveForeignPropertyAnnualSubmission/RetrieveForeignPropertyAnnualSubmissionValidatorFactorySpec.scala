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

package v4.retrieveForeignPropertyAnnualSubmission

import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v4.retrieveForeignPropertyAnnualSubmission.def1.Def1_RetrieveForeignPropertyAnnualSubmissionValidator
import v4.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

class RetrieveForeignPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"
  private val validTysTaxYear = "2023-24"

  private val validatorFactory = new RetrieveForeignPropertyAnnualSubmissionValidatorFactory

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTysTaxYear)

        result shouldBe a[Def1_RetrieveForeignPropertyAnnualSubmissionValidator]
      }

      "passed the minimum supported taxYear" in {
        val result: Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear)
        result shouldBe a[Def1_RetrieveForeignPropertyAnnualSubmissionValidator]

      }
    }
  }

}
