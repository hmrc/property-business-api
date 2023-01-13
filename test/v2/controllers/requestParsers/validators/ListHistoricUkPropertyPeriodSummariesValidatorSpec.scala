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
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRawData

class ListHistoricUkPropertyPeriodSummariesValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"

  private val validator = new ListHistoricUkPropertyPeriodSummariesValidator(mockAppConfig)

  "ListHistoricUkPropertyPeriodSummariesValidator" should {
    "return empty List (Nil)" when {
      "a valid raw data is supplied" in {
        validator.validate(ListHistoricUkPropertyPeriodSummariesRawData(validNino)) shouldBe Nil
      }
    }

    "return a validation error/s" when {
      "raw data contains invalid nino" in {
        validator.validate(ListHistoricUkPropertyPeriodSummariesRawData("AA12345")) shouldBe List(NinoFormatError)
      }

    }
  }
}
