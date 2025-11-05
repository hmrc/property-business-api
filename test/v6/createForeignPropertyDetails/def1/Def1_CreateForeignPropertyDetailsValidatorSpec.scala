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

  private val validPropertyName = "Bob & Bobby Co"
  private val validCountryCode  = "FRA"
  private val validEndDate      = "2026-08-24"
  private val validEndReason    = "no-longer-renting-property-out"

  private def entryWith(countryCode: String) = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$countryCode",
       |"endDate": "$validEndDate",
       |"endReason": "$validEndReason"
       |}
       |""".stripMargin
  )

  private def minimalEntryWith(countryCode: String) = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$countryCode"
       |}
       |""".stripMargin
  )

  private val bodyWithoutPropertyName = Json.parse(
    s"""
       |{
       |"countryCode": "$validCountryCode"
       |}
       |""".stripMargin
  )

  private val bodyWithoutCountryCode = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName"
       |}
       |""".stripMargin
  )

  private val bodyWithInvalidPropertyName = Json.parse(
    s"""
       |{
       |"propertyName": "",
       |"countryCode": "$validCountryCode",
       |"endDate": "$validEndDate",
       |"endReason": "$validEndReason"
       |}
       |""".stripMargin
  )

  private val bodyWithInvalidCountryCode = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "AB",
       |"endDate": "$validEndDate",
       |"endReason": "$validEndReason"
       |}
       |""".stripMargin
  )

  private val bodyWithInvalidEndDate = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$validCountryCode",
       |"endDate": "234-56-1",
       |"endReason": "$validEndReason"
       |}
       |""".stripMargin
  )

  private val bodyWithInvalidEndReason = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$validCountryCode",
       |"endDate": "$validEndDate",
       |"endReason": "not a valid reason"
       |}
       |""".stripMargin
  )

  private val bodyWithMissingEndDate = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$validCountryCode",
       |"endReason": "$validEndReason"
       |}
       |""".stripMargin
  )

  private val bodyWithMissingEndReason = Json.parse(
    s"""
       |{
       |"propertyName": "$validPropertyName",
       |"countryCode": "$validCountryCode",
       |"endDate": "$validEndDate"
       |}
       |""".stripMargin
  )

  private val validBody = entryWith(countryCode = validCountryCode)

  private val minimalValidBody = minimalEntryWith(countryCode = validCountryCode)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedBody = Def1_CreateForeignPropertyDetailsRequestBody(
    validPropertyName,
    validCountryCode,
    Some(validEndDate),
    Some(validEndReason)
  )

  private val minimalParsedBody = Def1_CreateForeignPropertyDetailsRequestBody(
    validPropertyName,
    validCountryCode,
    None,
    None
  )

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateForeignPropertyDetailsValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateForeignPropertyDetailsRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed the minimum supported taxYear" in {

        val taxYearString = "2026-27"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(Def1_CreateForeignPropertyDetailsRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }

      "passed a request with no 'endDate' and 'endReason'" in {
        val taxYearString = "2026-27"
        validator(validNino, validBusinessId, taxYearString, minimalValidBody).validateAndWrapResult() shouldBe
          Right(
            Def1_CreateForeignPropertyDetailsRequestData(
              parsedNino,
              parsedBusinessId,
              TaxYear.fromMtd(taxYearString),
              minimalParsedBody
            )
          )
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
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body without property name" in {
        val invalidBody = bodyWithoutPropertyName
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/propertyName")))
      }

      "passed a body without country code" in {
        val invalidBody = bodyWithoutCountryCode
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/countryCode")))
      }

      "passed a body with an endDate but no endReason" in {
        val invalidBody = bodyWithMissingEndReason
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an endReason but no endDate" in {
        val invalidBody = bodyWithMissingEndDate
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingEndDetailsError))
      }

      "passed a body with an invalid propertyName" in {
        val invalidBody = bodyWithInvalidPropertyName
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyNameFormatError))
      }

      "passed a body with an invalid countryCode" in {
        val invalidBody = bodyWithInvalidCountryCode
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError))
      }

      "passed a body with an invalid endDate" in {
        val invalidBody = bodyWithInvalidEndDate
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndDateFormatError))
      }

      "passed a body with an invalid endReason" in {
        val invalidBody = bodyWithInvalidEndReason
        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndReasonFormatError))
      }
    }

    "return multiple errors" when {
      "the path parameters have multiple issues" in {

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator("invalid", "invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError, TaxYearFormatError))
          )
        )
      }

      "passed a body with an invalidly formatted propertyName and missing end details" in {
        val requestWithInvalidPropertyNameAndMissingCountryCode = validBody.update("/propertyName", JsString("")).removeProperty("/endReason")

        val result: Either[ErrorWrapper, CreateForeignPropertyDetailsRequestData] =
          validator(validNino, validBusinessId, validTaxYear, requestWithInvalidPropertyNameAndMissingCountryCode).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(PropertyNameFormatError, RuleMissingEndDetailsError))))
      }
    }
  }

}
