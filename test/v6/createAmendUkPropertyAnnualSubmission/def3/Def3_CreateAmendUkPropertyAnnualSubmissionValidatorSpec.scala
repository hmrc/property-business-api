/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.createAmendUkPropertyAnnualSubmission.def3

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import common.models.errors.{RuleBothAllowancesSuppliedError, RuleBuildingNameNumberError}
import config.MockPropertyBusinessConfig
import play.api.libs.json.*
import v6.createAmendUkPropertyAnnualSubmission.def3.model.request.*
import v6.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

class Def3_CreateAmendUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2026-27"

  private val structuredBuildingAllowanceEntry = Json.parse("""
    |{
    |    "amount": 3000.30,
    |    "firstYear": {
    |      "qualifyingDate": "2020-01-01",
    |      "qualifyingAmountExpenditure": 3000.40
    |    },
    |    "building": {
    |      "name": "house name",
    |      "postcode": "GF4 9JH"
    |    }
    |}
    |""".stripMargin)

  private val enhancedStructuredBuildingAllowanceEntry = Json.parse("""
    |{
    | "amount": 3000.50,
    | "firstYear": {
    |   "qualifyingDate": "2020-01-01",
    |   "qualifyingAmountExpenditure": 3000.60
    | },
    | "building": {
    |   "number": "house number",
    |   "postcode": "GF4 9JH"
    | }
    |}
    |""".stripMargin)

  private def ukPropertyJson(structuredBuildingAllowanceEntries: JsValue*)(enhancedStructuredBuildingAllowance: JsValue*) = Json.parse(s"""
      |{
      |    "allowances": {
      |      "annualInvestmentAllowance": 2000.50,
      |      "zeroEmissionsGoodsVehicleAllowance": 2000.60,
      |      "businessPremisesRenovationAllowance": 2000.70,
      |      "otherCapitalAllowance": 2000.80,
      |      "costOfReplacingDomesticItems": 2000.90,
      |      "zeroEmissionsCarAllowance": 3000.80,
      |      "firstYearAllowanceOnPlantAndMachinery": 3000.90,
      |      "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowanceEntries)},
      |      "enhancedStructuredBuildingAllowance": ${JsArray(enhancedStructuredBuildingAllowance)}
      |    },
      |    "adjustments": {
      |      "balancingCharge": 2000.20,
      |      "privateUseAdjustment": 2000.30,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 2000.40,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |}
      |
      |""".stripMargin)

  private val validBody = Json.obj(
    "ukProperty" -> ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry)
  )

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedUkPropertyAdjustments =
    Adjustments(Some(2000.20), Some(2000.30), Some(2000.40), nonResidentLandlord = true, Some(RentARoom(true)))

  private val parsedUkPropertyAllowances =
    Allowances(
      Some(2000.50),
      Some(2000.60),
      Some(2000.70),
      Some(2000.80),
      Some(2000.90),
      Some(3000.80),
      Some(3000.90),
      None,
      Some(List(StructuredBuildingAllowance(3000.30, Some(FirstYear("2020-01-01", 3000.40)), Building(Some("house name"), None, "GF4 9JH")))),
      Some(List(StructuredBuildingAllowance(3000.50, Some(FirstYear("2020-01-01", 3000.60)), Building(None, Some("house number"), "GF4 9JH"))))
    )

  private val parsedUkProperty = UkProperty(Some(parsedUkPropertyAdjustments), Some(parsedUkPropertyAllowances))

  private val parsedBody = Def3_CreateAmendUkPropertyAnnualSubmissionRequestBody(parsedUkProperty)

  val submissionRequestBody: Def3_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def3_CreateAmendUkPropertyAnnualSubmissionRequestBody(
    ukProperty = parsedUkProperty
  )

  private def validator(nino: String, taxYear: String, businessId: String, body: JsValue): Def3_CreateAmendUkPropertyAnnualSubmissionValidator = {
    new Def3_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)
  }

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Right(Def3_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed the minimum supported taxYear" in new SetupConfig {
        val taxYearString = "2022-23"
        validator(validNino, taxYearString, validBusinessId, validBody).validateAndWrapResult() shouldBe
          Right(Def3_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted businessId" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, "invalid business id", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty body" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, Json.parse("""{}""")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with an empty object" should {
        def testEmpty(path: String): Unit =
          s"for $path" in new SetupConfig {
            val invalidBody: JsValue = validBody.removeProperty(path).replaceWithEmptyObject(path)

            val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
              validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

            result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(path)))
          }

        List(
          "/ukProperty",
          "/ukProperty/allowances",
          "/ukProperty/allowances/structuredBuildingAllowance",
          "/ukProperty/allowances/enhancedStructuredBuildingAllowance"
        ).foreach(p => testEmpty(p))
      }

      "passed a body with ukProperty adjustments missing a required field object" in new SetupConfig {
        val invalidBody: JsValue = validBody.removeProperty("/ukProperty/adjustments/nonResidentLandlord")

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukProperty/adjustments/nonResidentLandlord")))
      }

      "passed a body with ukProperty adjustments with an empty rentARoom object" in new SetupConfig {
        val invalidBody: JsValue = validBody.replaceWithEmptyObject("/ukProperty/adjustments/rentARoom/jointlyLet")

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukProperty/adjustments/rentARoom/jointlyLet")))
      }

      "passed a body with an invalid structuredBuildingAllowance/qualifyingDate" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))(
              enhancedStructuredBuildingAllowanceEntry)
          )
        validBody.update("/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate", JsString("2020.10.01"))

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, DateFormatError.withPath("/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid enhancedStructuredBuildingAllowance/qualifyingDate" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/name" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/name", JsString("*")))
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/name")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/number" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/number", JsString("")))
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/postcode" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/postcode", JsString("*")))
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode")))
      }

      "passed a body with invalid numeric fields" should {

        def testValueFormatError(path: String): Unit = s"for $path" in new SetupConfig {
          val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
            validator(validNino, validTaxYear, validBusinessId, validBody.update(path, JsNumber(123.456))).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        List(
          "/ukProperty/allowances/annualInvestmentAllowance",
          "/ukProperty/allowances/businessPremisesRenovationAllowance",
          "/ukProperty/allowances/otherCapitalAllowance",
          "/ukProperty/allowances/costOfReplacingDomesticItems",
          "/ukProperty/allowances/zeroEmissionsCarAllowance",
          "/ukProperty/allowances/firstYearAllowanceOnPlantAndMachinery",
          "/ukProperty/adjustments/balancingCharge",
          "/ukProperty/adjustments/privateUseAdjustment",
          "/ukProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
        ).foreach(p => testValueFormatError(p))
      }

      "passed a body with invalid structuredBuildingAllowance fields" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(
              structuredBuildingAllowanceEntry
                .update("/amount", JsNumber(3000.305342))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(3000.403423))
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukProperty/allowances/structuredBuildingAllowance/0/amount"
            ))
          ))
      }

      "passed a body with invalid enhancedStructuredBuildingAllowance fields" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .update("/amount", JsNumber(3000.5067))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(3000.6024)))
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/amount"
            ))
          ))
      }

      "passed a body with an invalid ukProperty propertyIncomeAllowance" in new SetupConfig {
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukProperty/allowances")
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(345.676))

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with ukProperty propertyIncomeAllowance that is too big" in new SetupConfig {
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukProperty/allowances")
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(1000.01))

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with both allowances and propertyIncomeAllowance supplied for non-fhl" in new SetupConfig {
        val invalidBody: JsValue =
          validBody
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukProperty/adjustments/privateUseAdjustment")

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/ukProperty/allowances")))
      }

      "passed a both with structuredBuildingAllowance/building with no name or number" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(
              structuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/ukProperty/allowances/structuredBuildingAllowance/0/building")))
      }

      "passed a both with enhancedStructuredBuildingAllowance/building with no name or number" in new SetupConfig {
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )
          )

        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError))
          )
        )
      }
    }
  }

}
