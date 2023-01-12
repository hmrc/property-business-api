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

import mocks.MockAppConfig
import support.UnitSpec
import v2.models.errors._
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRawData

class ListPropertyPeriodSummariesValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"

  MockAppConfig.minimumTaxV2Foreign returns 2021
  private val validator = new ListPropertyPeriodSummariesValidator(mockAppConfig)

  "ListPropertyPeriodSummariesValidator" should {
    "return empty List (Nil)" when {
      "a valid raw data is supplied" in {
        validator.validate(ListPropertyPeriodSummariesRawData(validNino, validBusinessId, validTaxYear)) shouldBe Nil
      }
    }

    "return a validation error/s" when {
      "raw data contains invalid nino" in {
        validator.validate(ListPropertyPeriodSummariesRawData("AA12345", validBusinessId, validTaxYear)) shouldBe List(NinoFormatError)
      }

      "raw data contains invalid tax year" in {
        validator.validate(ListPropertyPeriodSummariesRawData(validNino, validBusinessId, "202123")) shouldBe List(TaxYearFormatError)
      }

      "raw data contains invalid businessId" in {
        validator.validate(ListPropertyPeriodSummariesRawData(validNino, "XAIS12345678", validTaxYear)) shouldBe List(BusinessIdFormatError)
      }

      "raw data contains multiple errors" in {
        validator.validate(ListPropertyPeriodSummariesRawData("AA12345", "XAIS12345678", validTaxYear)) shouldBe List(NinoFormatError,
                                                                                                                      BusinessIdFormatError)
      }

      "raw data contains rule tax year errors" in {
        validator.validate(ListPropertyPeriodSummariesRawData(validNino, validBusinessId, "2018-19")) shouldBe List(RuleTaxYearNotSupportedError)
        validator.validate(ListPropertyPeriodSummariesRawData(validNino, validBusinessId, "2021-23")) shouldBe List(RuleTaxYearRangeInvalidError)
      }
    }
  }
}
