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

package v5.retrieveHistoricFhlUkPropertyAnnualSubmission

import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v5.retrieveHistoricFhlUkPropertyAnnualSubmission.def1.Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator
import v5.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData

class RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec {
  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validatorFactory = new RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory

  "validator()" when {

    "given any request regardless of tax year" should {
      "return the Validator for schema definition 1" in {
        val result: Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validTaxYear)

        result shouldBe a[Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator]
      }
    }

  }

}
