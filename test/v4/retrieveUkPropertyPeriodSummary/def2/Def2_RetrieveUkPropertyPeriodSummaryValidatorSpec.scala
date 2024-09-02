/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.retrieveUkPropertyPeriodSummary.def2

import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v4.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryValidatorFactory
import v4.retrieveUkPropertyPeriodSummary.model.request.{
  Def1_RetrieveUkPropertyPeriodSummaryRequestData,
  Def2_RetrieveUkPropertyPeriodSummaryRequestData,
  RetrieveUkPropertyPeriodSummaryRequestData
}

class Def2_RetrieveUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

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

  private val validatorFactory = new RetrieveUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String) =
    validatorFactory.validator(nino, businessId, taxYear, submissionId)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2022))

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTysTaxYear, parsedSubmissionId))
      }

      "given the minimum supported taxYear for Def2" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId))
      }

      "given the maximum supported taxYear" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2024-25", validSubmissionId).validateAndWrapResult()

        result shouldBe Right(
          Def2_RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd("2024-25"), parsedSubmissionId)
        )
      }
    }

    "return a single error" when {
      "given an invalid nino" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator("invalidNino", validBusinessId, validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid business ID" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalidBusinessId", validTysTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "given an incorrectly formatted tax year" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "invalidTaxYear", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "given a taxYear immediately before the minimum supported" in {
        setupMocks()

        val result = validator(validNino, validBusinessId, "2021-22", validSubmissionId).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "given a taxYear immediately after the maximum supported" in {
        setupMocks()

        val result = validator(validNino, validBusinessId, "2025-26", validSubmissionId).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "given an invalid tax year range" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "given an invalid submission ID" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTysTaxYear, "invalidSubmissionId").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return return multiple errors" when {
      "given invalid nino, business ID, tax year format and submission ID" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", "invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError, SubmissionIdFormatError, BusinessIdFormatError))
          )
        )
      }
    }
  }

}
