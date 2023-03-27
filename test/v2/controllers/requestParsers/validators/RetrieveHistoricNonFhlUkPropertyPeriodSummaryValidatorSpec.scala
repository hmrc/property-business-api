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
import api.models.errors._
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRawData

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  MockAppConfig.minimumTaxHistoric returns 2017
  MockAppConfig.maximumTaxHistoric returns 2021

  private val validator = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId)) shouldBe Nil
      }
    }
    "return a validation error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveHistoricNonFhlUkPiePeriodSummaryRawData("Nino", validPeriodId)) shouldBe List(NinoFormatError)
      }
      "an invalid periodId format is supplied" in {
        validator.validate(RetrieveHistoricNonFhlUkPiePeriodSummaryRawData(validNino, "2017-04-06__2017-07-04")) shouldBe List(PeriodIdFormatError)
      }
      "a non-historic periodId is supplied" in {
        validator.validate(RetrieveHistoricNonFhlUkPiePeriodSummaryRawData(validNino, "2012-04-06_2012-07-04")) shouldBe List(PeriodIdFormatError)
      }
      "multiple format errors are made" in {
        validator.validate(RetrieveHistoricNonFhlUkPiePeriodSummaryRawData("Nino", "2012-04-06_2012-07-04")) shouldBe
          List(NinoFormatError, PeriodIdFormatError)
      }
    }
  }
}
