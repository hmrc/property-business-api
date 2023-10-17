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
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, PeriodIdFormatError, RuleBothExpensesSuppliedError, RuleIncorrectOrEmptyBodyError, ValueFormatError}
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue}
import support.UnitSpec
import v2.fixtures.AmendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryFixtures
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPeriodSummaryRequestData

class AmendHistoricNonFhlUkPiePeriodSummaryValidatorFactorySpec
    extends UnitSpec
    with MockAppConfig
    with AmendHistoricNonFhlUkPiePeriodSummaryFixtures
    with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  private val parsedNino     = Nino(validNino)
  private val parsedPeriodId = PeriodId(validPeriodId)

  private val validatorFactory = new AmendHistoricNonFhlUkPeriodSummaryValidatorFactory(mockAppConfig)

  MockAppConfig.minimumTaxYearHistoric.returns(TaxYear.starting(2017))
  MockAppConfig.maximumTaxYearHistoric.returns(TaxYear.starting(2021))

  private def validator(nino: String, periodId: String, body: JsValue) =
    validatorFactory.validator(nino, periodId, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Right(AmendHistoricNonFhlUkPeriodSummaryRequestData(parsedNino, parsedPeriodId, requestBodyFull))
      }

      "passed a valid consolidated request" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestConsolidated).validateAndWrapResult()

        result shouldBe Right(AmendHistoricNonFhlUkPeriodSummaryRequestData(parsedNino, parsedPeriodId, requestBodyConsolidated))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator("invalid", validPeriodId, mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed a body with multiple invalid numeric amounts" in {
        val badNegativeValue = JsNumber(-4996.99)
        val badBigValue      = JsNumber(999999999999.99)

        val invalidBody = mtdJsonRequestFull
          .update("/income/taxDeducted", badNegativeValue)
          .update("/income/rentARoom/rentsReceived", badBigValue)
          .update("/expenses/premisesRunningCosts", badNegativeValue)
          .update("/expenses/rentARoom/amountClaimed", badBigValue)

        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List("/income/taxDeducted", "/income/rentARoom/rentsReceived", "/expenses/premisesRunningCosts", "/expenses/rentARoom/amountClaimed"))
          ))
      }

      "passed a body with both expenses supplied" in {
        val invalidBody = mtdJsonRequestFull
          .update("/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses"))
        )
      }

      "passed an empty body" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with empty income and expenses sub-objects" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestWithEmptySubObjects).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPaths(List("/income", "/expenses"))))
      }

      "passed a body with empty rentARoom sub-object" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, mtdJsonRequestWithEmptyRentARoom).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/income/rentARoom")))
      }

      "passed an invalidly formatted periodId" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, "20A7-04-06_2017-07-04", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed a periodId with a non-historic year" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed a periodId with a toDate before fromDate" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
          validator(validNino, "2019-07-04_2019-04-06", mtdJsonRequestFull).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPeriodSummaryRequestData] =
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
