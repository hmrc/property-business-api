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

package v4.propertyPeriodSummary.list.def1

import config.MockPropertyBusinessConfig
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v4.propertyPeriodSummary.list.model.request.ListPropertyPeriodSummariesRequestData

class Def1_ListPropertyPeriodSummariesValidatorSpec extends UnitSpec with MockPropertyBusinessConfig {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, businessId: String, taxYear: String) =
    new Def1_ListPropertyPeriodSummariesValidator(nino, businessId, taxYear)

  "validate()" should {
    "return the parsed domain object" when {
      "given a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
          validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Right(ListPropertyPeriodSummariesRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }

      "passed the minimum supported taxYear" in new SetupConfig {

        val taxYearString = "2021-22"
        validator(validNino, validBusinessId, taxYearString).validateAndWrapResult() shouldBe
          Right(ListPropertyPeriodSummariesRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString)))
      }
      "passed the maximum supported taxYear" in new SetupConfig {

        val taxYearString = "2024-25"
        validator(validNino, validBusinessId, taxYearString).validateAndWrapResult() shouldBe
          Right(ListPropertyPeriodSummariesRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString)))
      }
    }

    "return a single error" when {
      "given an invalid nino" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
          validator("5555", validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an incorrectly formatted taxYear" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
          validator(validNino, validBusinessId, "25667").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "given an incorrectly formatted businessId" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
          validator(validNino, "5555", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "given a taxYear immediately before the minimum supported" in new SetupConfig {

        validator(validNino, validBusinessId, "2020-21").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
      "given a taxYear immediately after max tax year" in new SetupConfig {

        validator(validNino, validBusinessId, "2025-26").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "given a taxYear spanning an invalid tax year range" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
          validator(validNino, validBusinessId, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {

        val result: Either[ErrorWrapper, ListPropertyPeriodSummariesRequestData] =
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
