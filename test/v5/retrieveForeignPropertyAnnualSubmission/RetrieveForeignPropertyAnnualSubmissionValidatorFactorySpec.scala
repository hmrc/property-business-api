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

package v5.retrieveForeignPropertyAnnualSubmission

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import mocks.MockAppConfig
import support.UnitSpec
import v5.retrieveForeignPropertyAnnualSubmission.def1.Def1_RetrieveForeignPropertyAnnualSubmissionValidator
import v5.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

class RetrieveForeignPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"
  private val validTysTaxYear = "2023-24"

  private val validatorFactory = new RetrieveForeignPropertyAnnualSubmissionValidatorFactory(mockAppConfig)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()
        val result: Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTysTaxYear)

        result shouldBe a[Def1_RetrieveForeignPropertyAnnualSubmissionValidator]
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val result: Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear)
        result shouldBe a[Def1_RetrieveForeignPropertyAnnualSubmissionValidator]

      }
    }
  }

}
