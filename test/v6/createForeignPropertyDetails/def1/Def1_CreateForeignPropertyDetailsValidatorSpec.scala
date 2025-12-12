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

package v6.createForeignPropertyDetails.def1

import play.api.libs.json.*
import shared.models.domain.*
import shared.models.errors.*
import common.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createForeignPropertyDetails.def1.model.request.{Def1_CreateForeignPropertyDetailsRequestBody, Def1_CreateForeignPropertyDetailsRequestData}
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

class Def1_CreateForeignPropertyDetailsValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2026-27"

  private val validBody = Json.parse(
    """
      |{
      |  "propertyName": "Bob & Bobby Co",
      |  "countryCode": "FRA",
      |  "endDate": "2026-08-24",
      |  "endReason": "no-longer-renting-property-out"
      |}
    """.stripMargin
  )

  private val minimalValidBody = validBody.removeProperty("/endDate").removeProperty("/endReason")

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedBody = Def1_CreateForeignPropertyDetailsRequestBody(
    "Bob & Bobby Co",
    "FRA",
    Some("2026-08-24"),
    Some("no-longer-renting-property-out")
  )

  private val minimalParsedBody = parsedBody.copy(endDate = None, endReason = None)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateForeignPropertyDetailsValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateForeignPropertyDetailsRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a request with no 'endDate' and 'endReason'" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, minimalValidBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateForeignPropertyDetailsRequestData(parsedNino, parsedBusinessId, parsedTaxYear, minimalParsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid business id" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a tax year with an invalid range" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, "2025-27", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an empty body" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body without property name" in {
        val invalidJson: JsValue = validBody.removeProperty("/propertyName")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/propertyName")))
      }

      "passed a body without country code" in {
        val invalidJson: JsValue = validBody.removeProperty("/countryCode")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/countryCode")))
      }

      "passed a body with an endDate but no endReason" in {
        val invalidJson: JsValue = validBody.removeProperty("/endReason")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an endReason but no endDate" in {
        val invalidJson: JsValue = validBody.removeProperty("/endDate")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an invalid propertyName" in {
        val invalidJson: JsValue = validBody.update("/propertyName", JsString(""))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyNameFormatError))
      }

      "passed a body with an invalid countryCode" in {
        val invalidJson: JsValue = validBody.update("/countryCode", JsString("FRANCE"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError))
      }

      "passed a body with an invalid ISO 3166-1 alpha-3 countryCode" in {
        val invalidJson: JsValue = validBody.update("/countryCode", JsString("ABC"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError))
      }

      "passed a body with an invalid endDate" in {
        val invalidJson: JsValue = validBody.update("/endDate", JsString("234-56-1"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndDateFormatError))
      }

      "passed a body with an invalid endReason" in {
        val invalidJson: JsValue = validBody.update("/endReason", JsString("not a valid reason"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndReasonFormatError))
      }

      "passed a body with an endDate that is before the start of the tax year" in {
        val invalidJson: JsValue = validBody.update("/endDate", JsString("2026-04-05"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleEndDateBeforeTaxYearStartError))
      }

      "passed a body with an endDate that is after the end of the tax year" in {
        val invalidJson: JsValue = validBody.update("/endDate", JsString("2027-04-06"))

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleEndDateAfterTaxYearEndError))
      }
    }

    "return multiple errors" when {
      "passed a body with an invalidly formatted propertyName and missing end details" in {
        val invalidJson: JsValue = validBody.update("/propertyName", JsString("")).removeProperty("/endReason")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(PropertyNameFormatError, RuleMissingEndDetailsError))))
      }
    }
  }

}
