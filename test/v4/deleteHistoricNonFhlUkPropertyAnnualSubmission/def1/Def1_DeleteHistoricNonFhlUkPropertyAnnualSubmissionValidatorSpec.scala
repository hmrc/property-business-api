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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.def1

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import config.MockAppConfig
import shared.utils.UnitSpec
import v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

class Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  implicit val correlationId: String = "X-123"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2019-20"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String) = new Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino, taxYear)

  private def setupMocks() = {
    MockedAppConfig.minimumTaxYearHistoric.returns(TaxYear.starting(2017))
    MockedAppConfig.maximumTaxYearHistoric.returns(TaxYear.starting(2021))
  }

  "validator" should {
    "return no errors" when {
      "a valid Fhl request is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear))
      }

      "a valid non-Fhl request is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear))
      }

      "passed the minimum supported taxYear" in allowsTaxYear("2017-18")
      "passed the maximum supported taxYear" in allowsTaxYear("2021-22")

      def allowsTaxYear(taxYearString: String): Unit = {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, taxYearString).validateAndWrapResult()
        result shouldBe Right(
          Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString))
        )
      }
    }

    "return a single error" when {
      "an invalid nino is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator("ABC", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "an invalid tax year format is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "20-21").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError, None))
      }

      "passed a taxYear immediately before the minimum supported" in disallowsTaxYear("2016-17")
      "passed a taxYear immediately after the maximum supported" in disallowsTaxYear("2022-23")

      def disallowsTaxYear(taxYearString: String): Unit = {
        setupMocks()

        val result = validator(validNino, taxYearString).validateAndWrapResult()
        result shouldBe
          Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()

        val result: Either[ErrorWrapper, DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }
  }

}
