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

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.MockAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v3.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData

class RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory(mockAppConfig)

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  MockedAppConfig.minimumTaxYearHistoric.returns(TaxYear.starting(2017))
  MockedAppConfig.maximumTaxYearHistoric.returns(TaxYear.starting(2021))

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear))
      }

      "passed the minimum supported taxYear" in allowsTaxYear("2017-18")
      "passed the maximum supported taxYear" in allowsTaxYear("2021-22")

      def allowsTaxYear(taxYearString: String): Unit =
        validator(validNino, taxYearString).validateAndWrapResult() shouldBe
          Right(RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString)))
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed a taxYear immediately before the minimum supported" in disallowsTaxYear("2016-17")
      "passed a taxYear immediately after the maximum supported" in disallowsTaxYear("2022-23")

      def disallowsTaxYear(taxYearString: String): Unit =
        validator(validNino, taxYearString).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))

      "passed a taxYear spanning an invalid tax year range" in {
        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
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
