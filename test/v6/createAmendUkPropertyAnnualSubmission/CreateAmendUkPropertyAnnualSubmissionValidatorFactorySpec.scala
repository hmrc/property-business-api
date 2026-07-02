/*
 * Copyright 2026 HM Revenue & Customs
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

import api.controllers.validators.{AlwaysErrorsValidator, Validator}
import api.utils.UnitSpec
import config.MockPropertyBusinessConfig
import play.api.libs.json.*
import v6.createAmendUkPropertyAnnualSubmission.def1.Def1_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.def2.Def2_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.def3.Def3_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

class CreateAmendUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private val validNino        = "AA123456B"
  private val validBusinessId  = "XAIS12345678901"
  private val validTaxYear     = "2022-23"
  private val validTysTaxYear  = "2023-24"
  private val validDef2TaxYear = "2025-26"
  private val validDef3TaxYear = "2026-27"
  private val validBody        = JsObject.empty

  private val validatorFactory = new CreateAmendUkPropertyAnnualSubmissionValidatorFactory

  "validator" when {
    "given a valid taxYear" should {
      "return the validator for schema definition 1" in new SetupConfig {
        val result: Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTysTaxYear, validBody)

        result shouldBe a[Def1_CreateAmendUkPropertyAnnualSubmissionValidator]
      }

      "return the validator for schema definition 2" in new SetupConfig {
        val result: Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validDef2TaxYear, validBody)

        result shouldBe a[Def2_CreateAmendUkPropertyAnnualSubmissionValidator]
      }

      "return the validator for schema definition 3" in new SetupConfig {
        val result: Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validDef3TaxYear, validBody)

        result shouldBe a[Def3_CreateAmendUkPropertyAnnualSubmissionValidator]
      }

      "return def1 when passed the minimum supported taxYear" in new SetupConfig {
        val result: Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, validBody)

        result shouldBe a[Def1_CreateAmendUkPropertyAnnualSubmissionValidator]
      }

      "return an error when given an invalid taxYear" in new SetupConfig {
        val result: Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, "2021-22", validBody)

        result shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
