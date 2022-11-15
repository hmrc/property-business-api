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
import v2.models.domain.HistoricPropertyType
import v2.models.errors._
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRawData

class DeleteHistoricUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"
  private val propertyType = HistoricPropertyType.Fhl

  MockAppConfig.minimumTaxHistoric returns 2020
  MockAppConfig.maximumTaxHistoric returns 2023
  private val validator = new DeleteHistoricUkPropertyAnnualSubmissionValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData(validNino, validTaxYear, propertyType)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData("ABC", validTaxYear, propertyType)) shouldBe List(NinoFormatError)
      }
      "an invalid tax year format is supplied" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData(validNino, "20-21", propertyType)) shouldBe List(TaxYearFormatError)
      }
      "a taxYear less than the minimum is supplied" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData(validNino, "2019-20", propertyType)) shouldBe List(
          RuleHistoricTaxYearNotSupportedError)
      }
      "a taxYear greater than the maximum is supplied" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData(validNino, "2024-25", propertyType)) shouldBe List(
          RuleHistoricTaxYearNotSupportedError)
      }
      "multiple format errors are made" in {
        validator.validate(DeleteHistoricUkPropertyAnnualSubmissionRawData("ABC", "21-22", propertyType)) shouldBe List(NinoFormatError,
                                                                                                                        TaxYearFormatError)
      }
    }
  }
}
