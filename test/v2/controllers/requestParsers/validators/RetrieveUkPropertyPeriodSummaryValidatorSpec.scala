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

package v2.controllers.requestParsers.validators

import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRawData

class RetrieveUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2022-23"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  MockAppConfig.minimumTaxV2Uk returns 2022
  private val validator = new RetrieveUkPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, validSubmissionId)) shouldBe Nil
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData("A12344A", validBusinessId, validTaxYear, validSubmissionId)) shouldBe
          List(NinoFormatError)
      }
    }

    "return BusinessIdFormatError" when {
      "an invalid business ID is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, "124", validTaxYear, validSubmissionId)) shouldBe
          List(BusinessIdFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year format is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, validBusinessId, "20178", validSubmissionId)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "an unsupported tax year is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, validBusinessId, "2021-22", validSubmissionId)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "an invalid tax year range is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, validBusinessId, "2022-24", validSubmissionId)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return SubmissionIdFormatError" when {
      "an invalid submission ID is supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, "X-124")) shouldBe
          List(SubmissionIdFormatError)
      }
    }

    "return return NinoFormatError, BusinessIdFormatError, TaxYearFormatError and SubmissionIdFormatError errors" when {
      "invalid nino, business ID, tax year format and submission ID are supplied" in {
        validator.validate(RetrieveUkPropertyPeriodSummaryRawData("A12344B", "ab-123-cd", "2021/22", "X/124")) shouldBe
          List(NinoFormatError, BusinessIdFormatError, TaxYearFormatError, SubmissionIdFormatError)
      }
    }
  }

}
