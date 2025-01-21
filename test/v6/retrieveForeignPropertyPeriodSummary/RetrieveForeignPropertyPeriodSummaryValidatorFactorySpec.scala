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

package v6.retrieveForeignPropertyPeriodSummary

import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyPeriodSummary.def1.Def1_RetrieveForeignPropertyPeriodSummaryValidator
import v6.retrieveForeignPropertyPeriodSummary.model.request._

class RetrieveForeignPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec {
  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2023-24"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val validatorFactory = new RetrieveForeignPropertyPeriodSummaryValidatorFactory

  "validator()" when {

    "given any request with a valid tax year" should {
      "return the Validator for schema definition 1" in {
        val result: Validator[RetrieveForeignPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, validSubmissionId)

        result shouldBe a[Def1_RetrieveForeignPropertyPeriodSummaryValidator]
      }
    }

  }

}
