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

import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequestData

class RetrieveUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2022-23"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val validatorFactory = new RetrieveUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String) =
    validatorFactory.validator(nino, businessId, taxYear, submissionId)

  class SetUp {

    MockAppConfig.minimumTaxV2Uk
      .returns(2022)
      .anyNumberOfTimes()

  }

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(RetrieveUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId))
      }
    }

    "return a single error" when {
      "an invalid nino is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator("5555", validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an invalid business ID is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "5555", validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "an incorrectly formatted tax year is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "24333", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an unsupported tax year is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2019-20", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "an invalid tax year range is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "an invalid submission ID is supplied" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, "5555").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return return multiple errors" when {
      "invalid nino, business ID, tax year format and submission ID are supplied" in new SetUp {
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
