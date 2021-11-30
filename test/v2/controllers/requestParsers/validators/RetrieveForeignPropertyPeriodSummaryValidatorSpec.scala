/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.MockAppConfig
import support.UnitSpec
import v2.models.errors._
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRawData

class RetrieveForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear = "2022-23"
  private val validSubmissionId = "12345678-1234-4123-9123-123456789012"

  MockAppConfig.minimumTaxV2Foreign returns 2022
  private val validator = new RetrieveForeignPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, validSubmissionId)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData("Walrus", validBusinessId, validTaxYear, validSubmissionId)) shouldBe List(NinoFormatError)
      }
      "an invalid businessId is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, "Beans", validTaxYear, validSubmissionId)) shouldBe List(BusinessIdFormatError)
      }
      "a taxYear with an invalid format is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2022/2023", validSubmissionId)) shouldBe List(TaxYearFormatError)
      }
      "a taxYear with an invalid range is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2022-24", validSubmissionId)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "an unsupported taxYear is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "2020-21", validSubmissionId)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "an invalid submissionId is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validTaxYear, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) shouldBe List(SubmissionIdFormatError)
      }
      "multiple format errors are made" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData("Walrus", "Beans", validTaxYear, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) shouldBe List(NinoFormatError, BusinessIdFormatError,SubmissionIdFormatError)
      }
    }
  }
}
