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

package v2.controllers.validators

import api.models.domain.{Nino, PeriodId, TaxYear}
import api.models.errors._
import config.MockAppConfig
import support.UnitSpec
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "X-12345"
  private val validNino                      = "AA123456A"
  private val validPeriodId                  = "2017-04-06_2017-07-04"

  private val parsedNino     = Nino("AA123456A")
  private val parsedPeriodId = PeriodId("2017-04-06_2017-07-04")

  MockedAppConfig.minimumTaxYearHistoric returns TaxYear.starting(2017)
  MockedAppConfig.maximumTaxYearHistoric returns TaxYear.starting(2021)

  private val validatorFactory = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, periodId: String) = validatorFactory.validator(nino, periodId)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, validPeriodId).validateAndWrapResult()

        result shouldBe Right(RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData(parsedNino, parsedPeriodId))
      }
    }
    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
          validator("invalid", validPeriodId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "passed an invalid periodId format is supplied" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }
      "passed an invalid period id" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }
      "a non-historic periodId is supplied" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }
    }
    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
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
