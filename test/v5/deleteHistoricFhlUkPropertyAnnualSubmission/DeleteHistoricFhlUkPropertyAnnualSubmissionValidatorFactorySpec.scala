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

package v5.deleteHistoricFhlUkPropertyAnnualSubmission

import common.models.domain.HistoricPropertyType
import config.MockPropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v5.deleteHistoricFhlUkPropertyAnnualSubmission.def1.Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionValidator
import v5.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

class DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  implicit val correlationId: String = "X-123"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2019-20"
  private val propertyType           = HistoricPropertyType.Fhl

  private val validatorFactory = new DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory

  "validator()" when {

    "given any request regardless of tax year" should {
      "return the Validator for schema definition 1" in new SetupConfig {
        val result: Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validTaxYear, propertyType)

        result shouldBe a[Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionValidator]
      }
    }
  }

}
