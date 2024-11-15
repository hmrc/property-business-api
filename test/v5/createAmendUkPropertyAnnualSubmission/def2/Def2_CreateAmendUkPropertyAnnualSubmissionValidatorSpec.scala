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

package v5.createAmendUkPropertyAnnualSubmission.def2

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v5.createAmendUkPropertyAnnualSubmission.def2.model.request._

class Def2_CreateAmendUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val structuredBuildingAllowanceEntry = Json.parse("""
      |{
      |    "amount": 234.34,
      |    "firstYear": {
      |      "qualifyingDate": "2020-03-29",
      |      "qualifyingAmountExpenditure": 3434.45
      |    },
      |    "building": {
      |      "name": "Plaza",
      |      "number": "1",
      |      "postcode": "TF3 4EH"
      |    }
      |}
      |""".stripMargin)

  private val enhancedStructuredBuildingAllowanceEntry = Json.parse("""
      |{
      | "amount": 234.45,
      | "firstYear": {
      |   "qualifyingDate": "2020-05-29",
      |   "qualifyingAmountExpenditure": 453.34
      | },
      | "building": {
      |   "name": "Plaza 2",
      |   "number": "2",
      |   "postcode": "TF3 4ER"
      | }
      |}
      |""".stripMargin)

  private def ukPropertyJson(structuredBuildingAllowanceEntries: JsValue*)(enhancedStructuredBuildingAllowance: JsValue*) = Json.parse(s"""
       |{
       |    "allowances": {
       |      "annualInvestmentAllowance": 678.45,
       |      "zeroEmissionsGoodsVehicleAllowance": 456.34,
       |      "businessPremisesRenovationAllowance": 573.45,
       |      "otherCapitalAllowance": 452.34,
       |      "costOfReplacingDomesticGoods": 567.34,
       |      "electricChargePointAllowance": 454.34,
       |      "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowanceEntries)},
       |      "enhancedStructuredBuildingAllowance": ${JsArray(enhancedStructuredBuildingAllowance)},
       |      "zeroEmissionsCarAllowance": 454.34
       |    },
       |    "adjustments": {
       |      "balancingCharge": 565.34,
       |      "privateUseAdjustment": 533.54,
       |      "businessPremisesRenovationAllowanceBalancingCharges": 563.34,
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
    Adjustments(Some(565.34), Some(533.54), Some(563.34), nonResidentLandlord = true, Some(RentARoom(true)))

  private val parsedUkPropertyAllowances = Allowances(
    Some(678.45),
    Some(456.34),
    Some(573.45),
    Some(452.34),
    Some(567.34),
    Some(454.34),
    None,
    Some(List(StructuredBuildingAllowance(234.34, Some(FirstYear("2020-03-29", 3434.45)), Building(Some("Plaza"), Some("1"), "TF3 4EH")))),
    Some(List(StructuredBuildingAllowance(234.45, Some(FirstYear("2020-05-29", 453.34)), Building(Some("Plaza 2"), Some("2"), "TF3 4ER"))))
  )

  private val parsedUkProperty = UkProperty(Some(parsedUkPropertyAdjustments), Some(parsedUkPropertyAllowances))

  private val parsedBody = Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody(parsedUkProperty)

  val submissionRequestBody: Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody(
    ukProperty = parsedUkProperty
  )

  private def validator(nino: String, taxYear: String, businessId: String, body: JsValue): Def2_CreateAmendUkPropertyAnnualSubmissionValidator = {
    new Def2_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)

    // new Def2_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)(mockAppConfig)
  }

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2021)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()

        val result = validator(validNino, validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2022-23"
        validator(validNino, taxYearString, validBusinessId, validBody).validateAndWrapResult() shouldBe
          Right(Def2_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        setupMocks()
        val result =
          validator("invalid nino", validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        setupMocks()
        val result =
          validator(validNino, "202324", validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()
        val result =
          validator(validNino, "2020-22", validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted businessId" in {
        setupMocks()
        val result =
          validator(validNino, validTaxYear, "invalid business id", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty body" in {
        setupMocks()
        val result =
          validator(validNino, validTaxYear, validBusinessId, Json.parse("""{}""")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with an empty object" should {
        def testEmpty(path: String): Unit =
          s"for $path" in {
            setupMocks()
            val invalidBody: JsValue = validBody.removeProperty(path).replaceWithEmptyObject(path)

            val result =
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

      "passed a body with ukProperty adjustments missing a required field object" in {
        setupMocks()
        val invalidBody: JsValue = validBody.removeProperty("/ukProperty/adjustments/nonResidentLandlord")

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukProperty/adjustments/nonResidentLandlord")))
      }

      "passed a body with ukProperty adjustments with an empty rentARoom object" in {
        setupMocks()
        val invalidBody: JsValue = validBody.replaceWithEmptyObject("/ukProperty/adjustments/rentARoom/jointlyLet")

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukProperty/adjustments/rentARoom/jointlyLet")))
      }

      "passed a body with an invalid structuredBuildingAllowance/qualifyingDate" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))(
              enhancedStructuredBuildingAllowanceEntry)
          )
        validBody.update("/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate", JsString("2020.10.01"))

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, DateFormatError.withPath("/ukProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid enhancedStructuredBuildingAllowance/qualifyingDate" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/name" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/name", JsString("*")))
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/name")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/number", JsString("")))
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/postcode" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/postcode", JsString("*")))
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode")))
      }

      "passed a body with invalid numeric fields" should {

        def testValueFormatError(path: String): Unit = s"for $path" in {
          setupMocks()
          val result =
            validator(validNino, validTaxYear, validBusinessId, validBody.update(path, JsNumber(123.456))).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        List(
          "/ukProperty/allowances/annualInvestmentAllowance",
          "/ukProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
          "/ukProperty/allowances/businessPremisesRenovationAllowance",
          "/ukProperty/allowances/otherCapitalAllowance",
          "/ukProperty/allowances/costOfReplacingDomesticGoods",
          "/ukProperty/allowances/zeroEmissionsCarAllowance",
          "/ukProperty/adjustments/balancingCharge",
          "/ukProperty/adjustments/privateUseAdjustment",
          "/ukProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
        ).foreach(p => testValueFormatError(p))
      }

      "passed a body with invalid structuredBuildingAllowance fields" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(
              structuredBuildingAllowanceEntry
                .update("/amount", JsNumber(234.345342))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(3434.453423))
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result =
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

      "passed a body with invalid enhancedStructuredBuildingAllowance fields" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .update("/amount", JsNumber(234.4576))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(453.3424)))
          )

        val result =
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

      "passed a body with an invalid ukProperty propertyIncomeAllowance" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukProperty/allowances")
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(345.676))

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with ukProperty propertyIncomeAllowance that is too big" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukProperty/allowances")
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(1000.01))

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with both allowances and propertyIncomeAllowance supplied for non-fhl" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .update("/ukProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukProperty/adjustments/privateUseAdjustment")

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/ukProperty/allowances")))
      }

      "passed a both with structuredBuildingAllowance/building with no name or number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(
              structuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/ukProperty/allowances/structuredBuildingAllowance/0/building")))
      }

      "passed a both with enhancedStructuredBuildingAllowance/building with no name or number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukProperty",
            ukPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )
          )

        val result =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/ukProperty/allowances/enhancedStructuredBuildingAllowance/0/building")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()
        val result =
          validator("invalid", "invalid", "invalid", validBody).validateAndWrapResult()

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
