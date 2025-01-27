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

package v5.propertyPeriodSummary.list

import config.MockPropertyBusinessConfig
import shared.utils.UnitSpec
import v5.propertyPeriodSummary.list.def1.Def1_ListPropertyPeriodSummariesValidator

class ListPropertyPeriodSummariesValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val invalidNino       = "not-a-nino"
  private val invalidBusinessId = "not-a-business-id"
  private val invalidTaxYear    = "not-a-tax-year"

  private val validatorFactory = new ListPropertyPeriodSummariesValidatorFactory

  "validator()" should {
    "return the Def1 Validator" when {
      "given any valid request" in new SetupConfig {
        val result = validatorFactory.validator(validNino, validBusinessId, validTaxYear)
        result shouldBe a[Def1_ListPropertyPeriodSummariesValidator]
      }

      "given any invalid request" in new SetupConfig {
        val result = validatorFactory.validator(invalidNino, invalidBusinessId, invalidTaxYear)
        result shouldBe a[Def1_ListPropertyPeriodSummariesValidator]
      }
    }
  }

}
