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

package v4.historicNonFhlUkPropertyPeriodSummary.amend.def1

import common.models.domain.PeriodId
import common.models.errors.{PeriodIdFormatError, RuleBothExpensesSuppliedError}
import config.MockPropertyBusinessConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue}
import shared.models.domain.Nino
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v4.historicNonFhlUkPropertyPeriodSummary.amend.def1.model.request.Def1_Fixtures
import v4.historicNonFhlUkPropertyPeriodSummary.amend.model.request.{
  AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData
}

class Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryValidatorSpec
    extends UnitSpec
    with MockPropertyBusinessConfig
    with JsonErrorValidators
    with Def1_Fixtures {
  private implicit val correlationId: String = "1234"

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  private val parsedNino     = Nino(validNino)
  private val parsedPeriodId = PeriodId(validPeriodId)

  private def validator(nino: String, periodId: String, body: JsValue) =
    new Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryValidator(nino, periodId, body)

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Right(Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedPeriodId, requestBodyFull))
      }

      "given a valid consolidated request" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestConsolidated).validateAndWrapResult()

        result shouldBe Right(Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedPeriodId, requestBodyConsolidated))
      }
    }

    "return a single error" when {
      "given an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid", validPeriodId, mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given a body with multiple invalid numeric amounts" in new SetupConfig {
        val badNegativeValue = JsNumber(-4996.99)
        val badBigValue      = JsNumber(999999999999.99)

        val invalidBody = mtdJsonRequestFull
          .update("/income/taxDeducted", badNegativeValue)
          .update("/income/rentARoom/rentsReceived", badBigValue)
          .update("/expenses/premisesRunningCosts", badNegativeValue)
          .update("/expenses/rentARoom/amountClaimed", badBigValue)

        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List(
                "/income/taxDeducted",
                "/income/rentARoom/rentsReceived",
                "/expenses/premisesRunningCosts",
                "/expenses/rentARoom/amountClaimed"
              )
            )
          ))
      }

      "given a body with both expenses supplied" in new SetupConfig {
        val invalidBody = mtdJsonRequestFull
          .update("/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses"))
        )
      }

      "given an empty body" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "given a body with empty income and expenses sub-objects" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestWithEmptySubObjects).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPaths(List("/income", "/expenses"))))
      }

      "given a body with empty rentARoom sub-object" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestWithEmptyRentARoom).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/income/rentARoom")))
      }

      "given an invalidly formatted periodId" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "20A7-04-06_2017-07-04", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given a periodId with a non-historic year" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "given a periodId with a toDate before fromDate" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2019-07-04_2019-04-06", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", mtdJsonRequestFull).validateAndWrapResult()

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
