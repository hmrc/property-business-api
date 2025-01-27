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

package v4.historicFhlUkPropertyPeriodSummary.retrieve.def1

import common.models.domain.PeriodId
import common.models.errors.PeriodIdFormatError
import config.MockPropertyBusinessConfig
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData
}

class Def1_RetrieveHistoricFhlUkPiePeriodSummaryValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {

  private implicit val correlationId: String = "X-12345"
  private val validNino                      = "AA123456A"
  private val validPeriodId                  = "2017-04-06_2017-07-04"

  private val parsedNino     = Nino("AA123456A")
  private val parsedPeriodId = PeriodId("2017-04-06_2017-07-04")

  private def validator(nino: String, periodId: String) = new Def1_RetrieveHistoricFhlUkPeriodSummaryValidator(nino, periodId)

  "validate()" should {
    "return the parsed domain object" when {
      "given a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedPeriodId))
      }
    }

    "return a single error" when {
      "given an invalid nino" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid", validPeriodId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid periodId format is supplied" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given an invalid period id" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given a non-historic periodId" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
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
