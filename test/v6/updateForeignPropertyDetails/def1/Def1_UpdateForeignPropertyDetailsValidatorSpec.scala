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

package v6.updateForeignPropertyDetails.def1

import common.models.domain.PropertyId
import play.api.libs.json.*
import shared.models.domain.*
import shared.models.errors.*
import common.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.updateForeignPropertyDetails.def1.model.Def1_UpdateForeignPropertyDetailsFixtures.*
import v6.updateForeignPropertyDetails.def1.model.request.Def1_UpdateForeignPropertyDetailsRequestData
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData

class Def1_UpdateForeignPropertyDetailsValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA999999A"
  private val validPropertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"
  private val validTaxYear    = "2026-27"

  private val parsedNino       = Nino(validNino)
  private val parsedPropertyId = PropertyId(validPropertyId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, propertyId: String, taxYear: String, body: JsValue) =
    new Def1_UpdateForeignPropertyDetailsValidator(nino, propertyId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {

        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, def1_UpdateForeignPropertyDetailsMtdJson).validateAndWrapResult()

        result shouldBe Right(
          Def1_UpdateForeignPropertyDetailsRequestData(parsedNino, parsedPropertyId, parsedTaxYear, def1_UpdateForeignPropertyDetailsModel))
      }

      "passed the minimum supported taxYear" in {

        val taxYearString = "2026-27"
        validator(validNino, validPropertyId, taxYearString, def1_UpdateForeignPropertyDetailsMtdJson).validateAndWrapResult() shouldBe
          Right(
            Def1_UpdateForeignPropertyDetailsRequestData(
              parsedNino,
              parsedPropertyId,
              TaxYear.fromMtd(taxYearString),
              def1_UpdateForeignPropertyDetailsModel))
      }

      "passed a request with no 'endDate' and 'endReason'" in {
        val taxYearString = "2026-27"
        validator(validNino, validPropertyId, taxYearString, def1_UpdateForeignPropertyDetailsMinimumMtdJson).validateAndWrapResult() shouldBe
          Right(
            Def1_UpdateForeignPropertyDetailsRequestData(
              parsedNino,
              parsedPropertyId,
              TaxYear.fromMtd(taxYearString),
              def1_UpdateForeignPropertyDetailsMinimumModel
            )
          )
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {

        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator("invalid nino", validPropertyId, validTaxYear, def1_UpdateForeignPropertyDetailsMtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid property id" in {

        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, "invalid", validTaxYear, def1_UpdateForeignPropertyDetailsMtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyIdFormatError))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body without property name" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.removeProperty("/propertyName")
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/propertyName")))
      }

      "passed a body with an endDate but no endReason" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.removeProperty("/endReason")
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an endReason but no endDate" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.removeProperty("/endDate")
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an invalid propertyName" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.update("/propertyName", JsString(""))
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyNameFormatError))
      }

      "passed a body with an invalid endDate" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.update("/endDate", JsString("2342-56-1"))
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndDateFormatError))
      }

      "passed a body with an invalid endReason" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.update("/endReason", JsString("Invalid"))
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndReasonFormatError))
      }

      "passed a body with an endDate after end of tax year" in {
        val invalidBody = def1_UpdateForeignPropertyDetailsMtdJson.update("/endDate", JsString("2027-08-24"))
        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleEndDateAfterTaxYearEndError))
      }
    }

    "return multiple errors" when {
      "the path parameters have multiple issues" in {

        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator("invalid", "invalid", validTaxYear, def1_UpdateForeignPropertyDetailsMtdJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, PropertyIdFormatError))
          )
        )
      }

      "passed a body with an invalidly formatted propertyName and missing end details" in {
        val requestWithInvalidPropertyNameAndMissingCountryCode =
          def1_UpdateForeignPropertyDetailsMtdJson.update("/propertyName", JsString("")).removeProperty("/endReason")

        val result: Either[ErrorWrapper, UpdateForeignPropertyDetailsRequestData] =
          validator(validNino, validPropertyId, validTaxYear, requestWithInvalidPropertyNameAndMissingCountryCode).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(PropertyNameFormatError, RuleMissingEndDetailsError))))
      }
    }
  }

}
