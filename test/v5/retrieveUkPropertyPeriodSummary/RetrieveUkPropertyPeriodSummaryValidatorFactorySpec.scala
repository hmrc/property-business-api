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

package v5.retrieveUkPropertyPeriodSummary

import config.MockPropertyBusinessConfig
import shared.utils.UnitSpec
import v5.retrieveUkPropertyPeriodSummary.def1.Def1_RetrieveUkPropertyPeriodSummaryValidator
import v5.retrieveUkPropertyPeriodSummary.def2.Def2_RetrieveUkPropertyPeriodSummaryValidator

class RetrieveUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val validatorFactory = new RetrieveUkPropertyPeriodSummaryValidatorFactory

  "validator()" when {
    "given a Def1 taxYear" should {
      "return the Def1 Validator" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validBusinessId, "2023-24", validSubmissionId)
        result shouldBe a[Def1_RetrieveUkPropertyPeriodSummaryValidator]
      }
    }

    "given a badly formatted taxYear" should {
      "return the Def1 Validator" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validBusinessId, "not-a-tax-year", validSubmissionId)
        result shouldBe a[Def1_RetrieveUkPropertyPeriodSummaryValidator]
      }
    }

    "given the Def2 start taxYear" should {
      "return the Def2 Validator" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validBusinessId, "2024-25", validSubmissionId)
        result shouldBe a[Def2_RetrieveUkPropertyPeriodSummaryValidator]
      }
    }

    "given a taxYear after the Def2 start" should {
      "return the Def2 Validator" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validBusinessId, "2025-26", validSubmissionId)
        result shouldBe a[Def2_RetrieveUkPropertyPeriodSummaryValidator]
      }
    }
  }

}
