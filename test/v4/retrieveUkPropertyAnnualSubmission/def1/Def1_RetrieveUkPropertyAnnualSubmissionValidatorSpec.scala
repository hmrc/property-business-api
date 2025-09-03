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

package v4.retrieveUkPropertyAnnualSubmission.def1

import config.MockPropertyBusinessConfig
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v4.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionValidatorFactory
import v4.retrieveUkPropertyAnnualSubmission.model.request.{
  Def1_RetrieveUkPropertyAnnualSubmissionRequestData,
  RetrieveUkPropertyAnnualSubmissionRequestData
}

class Def1_RetrieveUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockPropertyBusinessConfig {

  implicit private val correlationId: String = "X-123"
  private val validNino                      = "AA123456B"
  private val validBusinessId                = "XAIS12345678901"
  private val validTaxYear                   = "2023-24"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveUkPropertyAnnualSubmissionValidatorFactory

  private def validator(nino: String, businessId: String, taxYear: String) =
    validatorFactory.validator(nino, businessId, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }

      "given the minimum supported taxYear" in new SetupConfig {
        val taxYearString = "2022-23"

        val result = validator(validNino, validBusinessId, taxYearString).validateAndWrapResult()

        result shouldBe Right(
          Def1_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString))
        )
      }

      "given the maximum supported taxYear" in new SetupConfig {
        val taxYearString = "2024-25"

        val result = validator(validNino, validBusinessId, taxYearString).validateAndWrapResult()

        result shouldBe Right(
          Def1_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString))
        )
      }
    }

    "return a single validation error" when {
      "given an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "given an incorrectly formatted taxYear" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "given an incorrectly formatted businessId" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "given a taxYear immediately before the minimum supported" in new SetupConfig {

        val result = validator(validNino, validBusinessId, "2021-22").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "given a taxYear immediately after the maximum supported" in new SetupConfig {

        val result = validator(validNino, validBusinessId, "2025-26").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "given a taxYear spanning an invalid range" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2021-23").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

    }

    "return multiple validation errors" when {
      "given a request with multiple issues (path parameters)" in new SetupConfig {

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }
  }

}
