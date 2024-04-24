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

package v4.deleteHistoricFhlUkPropertyAnnualSubmission.def1

import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v4.deleteHistoricFhlUkPropertyAnnualSubmission.model.request._

class Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig {

  implicit val correlationId: String = "X-123"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2019-20"
  private val propertyType           = HistoricPropertyType.Fhl

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String, propertyType: HistoricPropertyType) =
    new Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionValidator(nino, taxYear, propertyType, mockAppConfig)

  def setupMocks() = {
    MockedAppConfig.minimumTaxYearHistoric.returns(TaxYear.starting(2017))
    MockedAppConfig.maximumTaxYearHistoric.returns(TaxYear.starting(2021))
  }

  "validator" should {
    "return no errors" when {
      "a valid Fhl request is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, HistoricPropertyType.Fhl).validateAndWrapResult()

        result shouldBe Right(Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, HistoricPropertyType.Fhl))
      }

      "a valid non-Fhl request is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, HistoricPropertyType.NonFhl).validateAndWrapResult()

        result shouldBe Right(Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, HistoricPropertyType.NonFhl))
      }

      "passed the minimum supported taxYear" in allowsTaxYear("2017-18")
      "passed the maximum supported taxYear" in allowsTaxYear("2021-22")

      def allowsTaxYear(taxYearString: String): Unit = {
        setupMocks()
        validator(validNino, taxYearString, propertyType).validateAndWrapResult() shouldBe
          Right(Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString), propertyType))
      }
    }

    "return a single error" when {
      "an invalid nino is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator("ABC", validTaxYear, propertyType).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "an invalid tax year format is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "20-21", propertyType).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError, None))
      }

      "passed a taxYear immediately before the minimum supported" in disallowsTaxYear("2016-17")
      "passed a taxYear immediately after the maximum supported" in disallowsTaxYear("2022-23")

      def disallowsTaxYear(taxYearString: String): Unit = {
        setupMocks()
        validator(validNino, taxYearString, propertyType).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2020-22", propertyType).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()
        val result: Either[ErrorWrapper, DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", propertyType).validateAndWrapResult()

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
