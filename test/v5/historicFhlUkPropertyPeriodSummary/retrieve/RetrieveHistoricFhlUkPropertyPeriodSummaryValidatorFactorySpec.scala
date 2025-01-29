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

package v5.historicFhlUkPropertyPeriodSummary.retrieve

import config.MockPropertyBusinessConfig
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v5.historicFhlUkPropertyPeriodSummary.retrieve.def1.Def1_RetrieveHistoricFhlUkPeriodSummaryValidator

class RetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {

  private val validNino = "AA123456A"
  private val periodId  = "2017-04-06_2017-07-04"

  private val invalidNino = "not-a-nino"

  private val validatorFactory = new RetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory

  "validator" should {
    "return the Def1 validator" when {
      "given any valid request" in new SetupConfig {
        val result = validatorFactory.validator(validNino, periodId)
        result shouldBe a[Def1_RetrieveHistoricFhlUkPeriodSummaryValidator]
      }

      "given any invalid request" in new SetupConfig {
        val result = validatorFactory.validator(invalidNino, periodId)
        result shouldBe a[Def1_RetrieveHistoricFhlUkPeriodSummaryValidator]
      }
    }
  }

}
