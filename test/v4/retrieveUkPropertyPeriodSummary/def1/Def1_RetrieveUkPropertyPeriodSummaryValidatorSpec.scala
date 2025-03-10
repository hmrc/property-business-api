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

package v4.retrieveUkPropertyPeriodSummary.def1

import common.models.domain.SubmissionId
import common.models.errors.SubmissionIdFormatError
import config.MockPropertyBusinessConfig
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v4.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryValidatorFactory
import v4.retrieveUkPropertyPeriodSummary.model.request.{Def1_RetrieveUkPropertyPeriodSummaryRequestData, RetrieveUkPropertyPeriodSummaryRequestData}

class Def1_RetrieveUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockPropertyBusinessConfig {

  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2022-23"
  private val validTysTaxYear   = "2023-24"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedTysTaxYear   = TaxYear.fromMtd(validTysTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val validatorFactory = new RetrieveUkPropertyPeriodSummaryValidatorFactory

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String) =
    validatorFactory.validator(nino, businessId, taxYear, submissionId)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTysTaxYear, parsedSubmissionId))
      }

      "passed the minimum supported taxYear" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId))
      }

      "passed the maximum supported taxYear" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2023-24", validSubmissionId).validateAndWrapResult()

        result shouldBe Right(
          Def1_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd("2023-24"), parsedSubmissionId))
      }
    }

    "return a single error" when {
      "an invalid nino is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator("invalidNino", validBusinessId, validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an invalid business ID is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalidBusinessId", validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "an incorrectly formatted tax year is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "invalidTaxYear", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2021-22", validSubmissionId).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear immediately after the maximum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2025-26", validSubmissionId).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "an invalid tax year range is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "an invalid submission ID is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTysTaxYear, "invalidSubmissionId").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return return multiple errors" when {
      "invalid nino, business ID, tax year format and submission ID are supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", "invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError, SubmissionIdFormatError, TaxYearFormatError))
          )
        )
      }
    }
  }

}
