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

package v4.historicNonFhlUkPropertyPeriodSummary.retrieve

import shared.utils.UnitSpec
import v4.historicNonFhlUkPropertyPeriodSummary.retrieve.def1.Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec {

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  private val invalidNino     = "not-a-nino"
  private val invalidPeriodId = "not-a-period-id"

  private val validatorFactory = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory

  "validator()" should {
    "return the Def1 validator" when {

      "given any valid request" in {
        val result = validatorFactory.validator(validNino, validPeriodId)
        result shouldBe a[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator]
      }

      "given any invalid request" in {
        val result = validatorFactory.validator(invalidNino, invalidPeriodId)
        result shouldBe a[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator]
      }
    }
  }

}
