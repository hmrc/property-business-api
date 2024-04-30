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

package v4.retrieveHistoricFhlUkPropertyPeriodSummary.def1

import api.models.domain.{Nino, PeriodId, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import support.UnitSpec
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.request.{Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData, RetrieveHistoricFhlUkPiePeriodSummaryRequestData}

class Def1_RetrieveHistoricFhlUkPiePeriodSummaryValidatorFactorySpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "X-12345"
  private val validNino                      = "AA123456A"
  private val validPeriodId                  = "2017-04-06_2017-07-04"

  private val parsedNino     = Nino("AA123456A")
  private val parsedPeriodId = PeriodId("2017-04-06_2017-07-04")

  private def setupMocks() = {
    MockedAppConfig.minimumTaxYearHistoric returns TaxYear.starting(2017)
    MockedAppConfig.maximumTaxYearHistoric returns TaxYear.starting(2021)
  }

  private def validator(nino: String, periodId: String) = new Def1_RetrieveHistoricFhlUkPeriodSummaryValidator(nino, periodId, mockAppConfig)

  "validate()" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, validPeriodId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData(parsedNino, parsedPeriodId))
      }
    }

    "return a single error" when {
      "given an invalid nino" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator("invalid", validPeriodId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid periodId format is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given an invalid period id" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given a non-historic periodId" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
          validator("invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, PeriodIdFormatError))
          )
        )
      }
    }

  }

}
