/*
 * Copyright 2022 HM Revenue & Customs
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
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData

class RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2021-22"

  MockAppConfig.minimumTaxV2Uk returns 2021

  private val validator = new RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData("Nino", validTaxYear)) shouldBe List(NinoFormatError)
      }
      "an invalid taxYear format is supplied" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, "2021/22")) shouldBe List(TaxYearFormatError)
      }
      "an unsupported taxYear is supplied" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, "2019-20")) shouldBe List(
          RuleTaxYearNotSupportedError)
      }
      "an invalid taxYear range is supplied" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData(validNino, "2021-23")) shouldBe List(
          RuleTaxYearRangeInvalidError)
      }
      "multiple format errors are made" in {
        validator.validate(RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData("Nino", "2021/22")) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}
