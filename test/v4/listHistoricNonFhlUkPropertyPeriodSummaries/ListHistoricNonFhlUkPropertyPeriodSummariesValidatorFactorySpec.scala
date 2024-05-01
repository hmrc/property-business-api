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

package v4.listHistoricNonFhlUkPropertyPeriodSummaries

import mocks.MockAppConfig
import support.UnitSpec
import v4.listHistoricNonFhlUkPropertyPeriodSummaries.def1.Def1_ListHistoricNonFhlUkPropertyPeriodSummariesValidator

class ListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino   = "AA123456A"
  private val invalidNino = "not-a-nino"

  private val validatorFactory = new ListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory

  "validator()" should {
    "return the Def1 validator" when {

      "given any valid request" in {
        val result = validatorFactory.validator(validNino)
        result shouldBe a[Def1_ListHistoricNonFhlUkPropertyPeriodSummariesValidator]
      }

      "given any invalid request" in {
        val result = validatorFactory.validator(invalidNino)
        result shouldBe a[Def1_ListHistoricNonFhlUkPropertyPeriodSummariesValidator]
      }
    }
  }

}
