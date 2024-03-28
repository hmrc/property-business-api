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

package v4.controllers.retrieveUkPropertyAnnualSubmission.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v4.controllers.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionValidatorFactory
import v4.controllers.retrieveUkPropertyAnnualSubmission.model.request.{
  Def1_RetrieveUkPropertyAnnualSubmissionRequestData,
  RetrieveUkPropertyAnnualSubmissionRequestData
}

class Def1_RetrieveUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  implicit private val correlationId: String = "X-123"
  private val validNino                      = "AA123456B"
  private val validBusinessId                = "XAIS12345678901"
  private val validTaxYear                   = "2023-24"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveUkPropertyAnnualSubmissionValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String) =
    validatorFactory.validator(nino, businessId, taxYear)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2022)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "valid request data is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2022-23"
        validator(validNino, validBusinessId, taxYearString).validateAndWrapResult() shouldBe
          Right(Def1_RetrieveUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString)))
      }
    }

    "return a single validation error" when {
      "an invalid nino is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "passed an incorrectly formatted taxYear" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        setupMocks()
        validator(validNino, validBusinessId, "2021-22").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2021-23").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

    }

    "return multiple validation errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()
        val result: Either[ErrorWrapper, RetrieveUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError, BusinessIdFormatError))
          )
        )
      }
    }
  }

}
