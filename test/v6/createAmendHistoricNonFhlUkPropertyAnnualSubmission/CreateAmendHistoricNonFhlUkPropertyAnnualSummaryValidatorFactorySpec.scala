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

package v6.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import config.MockPropertyBusinessConfig
import play.api.libs.json._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator

class CreateAmendHistoricNonFhlUkPropertyAnnualSummaryValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validatorFactory =
    new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory()

  "validator" should {
    "return the Def1 validator" when {
      "given any request" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validTaxYear, JsObject.empty)
        result shouldBe a[Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator]
      }
    }
  }

}
