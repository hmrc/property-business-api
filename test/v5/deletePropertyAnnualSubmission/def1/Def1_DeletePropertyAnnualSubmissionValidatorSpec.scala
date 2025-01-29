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

package v5.deletePropertyAnnualSubmission.def1

import config.MockPropertyBusinessConfig
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v5.deletePropertyAnnualSubmission.DeletePropertyAnnualSubmissionValidatorFactory
import v5.deletePropertyAnnualSubmission.model.request._

class Def1_DeletePropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockPropertyBusinessConfig {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"
  private val validTysTaxYear = "2023-24"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedTysTaxYear = TaxYear.fromMtd(validTysTaxYear)

  private val validatorFactory = new DeletePropertyAnnualSubmissionValidatorFactory

  private def validator(nino: String, businessId: String, taxYear: String) = validatorFactory.validator(nino, businessId, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTysTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_DeletePropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTysTaxYear))
      }

      "passed the minimum supported taxYear" in new SetupConfig {
        validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult() shouldBe
          Right(Def1_DeletePropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTysTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTysTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2020-21").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {
        val result: Either[ErrorWrapper, DeletePropertyAnnualSubmissionRequestData] =
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
