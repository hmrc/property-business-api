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

package v5.retrieveUkPropertyAnnualSubmission.def2

import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import config.MockAppConfig
import shared.utils.UnitSpec
import v5.retrieveUkPropertyAnnualSubmission.def2.model.Def2_RetrieveUkPropertyAnnualSubmissionValidator
import v5.retrieveUkPropertyAnnualSubmission.def2.model.request.Def2_RetrieveUkPropertyAnnualSubmissionRequestData
import v5.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData

class Def2_RetrieveUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  implicit private val correlationId: String = "X-123"
  private val validNino                      = "AA123456B"
  private val validBusinessId                = "XAIS12345678901"
  private val validTaxYear                   = "2025-26"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, businessId: String, taxYear: String) =
    new Def2_RetrieveUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2022)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def2_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }

      "given the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2025-26"

        val result = validator(validNino, validBusinessId, taxYearString).validateAndWrapResult()

        result shouldBe Right(
          Def2_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString))
        )
      }
    }

    "return a single validation error" when {
      "given an invalid nino" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "given an incorrectly formatted taxYear" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "given an incorrectly formatted businessId" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "given a taxYear spanning an invalid range" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2021-23").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

    }

    "return multiple validation errors" when {
      "given a request with multiple issues (path parameters)" in {
        setupMocks()

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
