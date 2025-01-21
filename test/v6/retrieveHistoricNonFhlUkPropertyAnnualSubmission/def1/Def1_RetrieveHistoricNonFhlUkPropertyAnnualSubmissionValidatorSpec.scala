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

package v6.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v6.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
import v6.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

class Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec {

  implicit val correlationId: String = "X-12345"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2019-20"

  private val parsedNino    = Nino("AA123456A")
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory()

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear))
      }

      "given the minimum supported taxYear" in allowsTaxYear("2017-18")
      "given the maximum supported taxYear" in allowsTaxYear("2021-22")

      def allowsTaxYear(taxYearString: String): Unit = {
        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, taxYearString).validateAndWrapResult()
        result shouldBe Right(
          Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString))
        )
      }
    }

    "return a single error" when {
      "given an invalid nino" in {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid tax year format" in {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2021/22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "given a taxYear immediately before the minimum supported" in disallowsTaxYear("2016-17")
      "given a taxYear immediately after the maximum supported" in disallowsTaxYear("2022-23")

      def disallowsTaxYear(taxYearString: String): Unit = {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, taxYearString).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))
      }

      "given an invalid taxYear range" in {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2021-23").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {

        val result: Either[ErrorWrapper, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
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
