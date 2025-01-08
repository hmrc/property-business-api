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

package v3.controllers.validators

import common.models.domain.SubmissionId
import common.models.errors.SubmissionIdFormatError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import config.MockAppConfig
import shared.utils.UnitSpec
import v3.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequestData

class RetrieveForeignPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2023-24"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val validatorFactory = new RetrieveForeignPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String) =
    validatorFactory.validator(nino, businessId, taxYear, submissionId)

  MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021))

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Right(RetrieveForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId))
      }

      "passed the minimum supported taxYear" in {
        val taxYearString = "2021-22"
        validator(validNino, validBusinessId, taxYearString, validSubmissionId).validateAndWrapResult() shouldBe
          Right(RetrieveForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedSubmissionId))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "202324", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid business id", validTaxYear, validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        validator(validNino, validBusinessId, "2020-21", validSubmissionId).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validSubmissionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted submissionId" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, "invalid submission id").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyPeriodSummaryRequestData] =
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
