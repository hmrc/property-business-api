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

package v4.createAmendUkPropertyAnnualSubmission.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty.{
  Def1_Create_Amend_UkFhlProperty,
  Def1_Create_Amend_UkFhlPropertyAdjustments,
  Def1_Create_Amend_UkFhlPropertyAllowances
}
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukNonFhlProperty.{
  Def1_Create_Amend_Building,
  Def1_Create_Amend_FirstYear,
  Def1_Create_Amend_StructuredBuildingAllowance,
  Def1_Create_Amend_UkNonFhlProperty,
  Def1_Create_Amend_UkNonFhlPropertyAdjustments,
  Def1_Create_Amend_UkNonFhlPropertyAllowances
}
import v4.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukPropertyRentARoom.Def1_Create_Amend_UkPropertyAdjustmentsRentARoom
import v4.createAmendUkPropertyAnnualSubmission.model.request.{
  Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendUkPropertyAnnualSubmissionRequestData
}

class Def1_CreateAmendUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val validBodyMinimal = Json.parse("""
      |{
      |  "ukFhlProperty": {
      |    "adjustments": {
      |      "periodOfGraceAdjustment": true,
      |      "nonResidentLandlord": true
      |    }
      |  }
      |}
      |""".stripMargin)

  private val ukFhlPropertyJson = Json.parse("""
      |{
      | "allowances": {
      |   "annualInvestmentAllowance": 123.45,
      |   "businessPremisesRenovationAllowance": 345.56,
      |   "otherCapitalAllowance": 345.34,
      |   "electricChargePointAllowance": 453.34,
      |   "zeroEmissionsCarAllowance": 123.12
      |  },
      |  "adjustments": {
      |   "privateUseAdjustment": 454.45,
      |   "balancingCharge": 231.45,
      |   "periodOfGraceAdjustment": true,
      |   "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
      |   "nonResidentLandlord": true,
      |   "rentARoom": {
      |     "jointlyLet": true
      |   }
      | }
      |}
      |""".stripMargin)

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

  private def ukNonFhlPropertyJson(structuredBuildingAllowanceEntries: JsValue*)(enhancedStructuredBuildingAllowance: JsValue*) = Json.parse(s"""
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
    "ukFhlProperty"    -> ukFhlPropertyJson,
    "ukNonFhlProperty" -> ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry)
  )

  private val validBodyFhlOnly = Json.obj("ukFhlProperty" -> ukFhlPropertyJson)

  private val validBodyNonFhlOnly =
    Json.obj("ukNonFhlProperty" -> ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry))

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedUkFhlPropertyAdjustments = Def1_Create_Amend_UkFhlPropertyAdjustments(
    Some(454.45),
    Some(231.45),
    periodOfGraceAdjustment = true,
    Some(567.67),
    nonResidentLandlord = true,
    Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true))
  )

  private val parsedUkFhlPropertyAllowances: Def1_Create_Amend_UkFhlPropertyAllowances =
    Def1_Create_Amend_UkFhlPropertyAllowances(Some(123.45), Some(345.56), Some(345.34), Some(453.34), Some(123.12), None)

  private val parsedUkFhlProperty =
    Def1_Create_Amend_UkFhlProperty(
      Some(parsedUkFhlPropertyAdjustments),
      Some(parsedUkFhlPropertyAllowances)
    )

  private val parsedUkNonFhlPropertyAdjustments =
    Def1_Create_Amend_UkNonFhlPropertyAdjustments(
      Some(565.34),
      Some(533.54),
      Some(563.34),
      nonResidentLandlord = true,
      Some(Def1_Create_Amend_UkPropertyAdjustmentsRentARoom(true)))

  //@formatter:off
  private val parsedUkNonFhlPropertyAllowances = Def1_Create_Amend_UkNonFhlPropertyAllowances(
    Some(678.45), Some(456.34), Some(573.45), Some(452.34),
    Some(567.34), Some(454.34), Some(454.34), None,
    Some(List(Def1_Create_Amend_StructuredBuildingAllowance(234.34, Some(Def1_Create_Amend_FirstYear("2020-03-29", 3434.45)), Def1_Create_Amend_Building(Some("Plaza"), Some("1"), "TF3 4EH")))),
    Some(List(Def1_Create_Amend_StructuredBuildingAllowance(234.45, Some(Def1_Create_Amend_FirstYear("2020-05-29", 453.34)), Def1_Create_Amend_Building(Some("Plaza 2"), Some("2"), "TF3 4ER"))))
  )
  //@formatter:on

  private val parsedUkProperty = Def1_Create_Amend_UkNonFhlProperty(Some(parsedUkNonFhlPropertyAdjustments), Some(parsedUkNonFhlPropertyAllowances))

  private val parsedUkFhlPropertyMinimal =
    Def1_Create_Amend_UkFhlProperty(
      Some(Def1_Create_Amend_UkFhlPropertyAdjustments(None, None, periodOfGraceAdjustment = true, None, nonResidentLandlord = true, None)),
      None)

  private val parsedBody = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(Some(parsedUkFhlProperty), Some(parsedUkProperty))

  private val parsedBodyMinimal = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(Some(parsedUkFhlPropertyMinimal), None)

  val submissionRequestBody: Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(
    ukFhlProperty = Some(parsedUkFhlProperty),
    ukNonFhlProperty = Some(parsedUkProperty)
  )

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)(mockAppConfig)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2021)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with a minimal request body" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, validTaxYear, validBodyMinimal).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimal))
      }

      "passed a valid request where only a ukFhlProperty is supplied" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, validTaxYear, validBodyFhlOnly).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedBody.copy(ukNonFhlProperty = None)))
      }

      "passed a valid request where only a ukNonFhlProperty is supplied" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, validTaxYear, validBodyNonFhlOnly).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody.copy(ukFhlProperty = None)))
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2022-23"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        setupMocks()
        val result =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, "202324", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, "2020-22", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        setupMocks()
        validator(validNino, validBusinessId, "2020-21", validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an incorrectly formatted businessId" in {
        setupMocks()
        val result =
          validator(validNino, "invalid business id", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty body" in {
        setupMocks()
        val result =
          validator(validNino, validBusinessId, validTaxYear, Json.parse("""{}""")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with an empty object" should {
        def testEmpty(path: String): Unit =
          s"for $path" in {
            setupMocks()
            val invalidBody: JsValue = validBody.removeProperty(path).replaceWithEmptyObject(path)

            val result =
              validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

            result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(path)))
          }

        List(
          "/ukFhlProperty",
          "/ukFhlProperty/allowances",
          "/ukNonFhlProperty",
          "/ukNonFhlProperty/allowances",
          "/ukNonFhlProperty/allowances/structuredBuildingAllowance",
          "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance"
        ).foreach(p => testEmpty(p))
      }

      "passed a body with an empty object except for an additional (non-schema) property" in {
        setupMocks()
        val invalidBody: JsValue = Json.parse("""
            |{
            |    "ukFhlProperty":{
            |       "unknownField": 999.99
            |    }
            |}""".stripMargin)

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukFhlProperty")))
      }

      "passed a body with an empty ukFhlPropertyAdjustments object" in {
        setupMocks()
        val invalidBody: JsValue = Json.parse("""
            |{
            |  "ukFhlProperty": {
            |      "adjustments": {}
            |  }
            |}
            |""".stripMargin)

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(
              List("/ukFhlProperty/adjustments/nonResidentLandlord", "/ukFhlProperty/adjustments/periodOfGraceAdjustment"))
          ))
      }

      "passed a body with a ukFhlPropertyAdjustments containing an empty rentARoom object" in {
        setupMocks()
        val invalidBody: JsValue = validBody.removeProperty("/ukFhlProperty/adjustments/rentARoom/jointlyLet")

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukFhlProperty/adjustments/rentARoom/jointlyLet")))
      }

      "passed a body with ukNonFhlProperty adjustments missing a required field object" in {
        setupMocks()
        val invalidBody: JsValue = validBody.removeProperty("/ukNonFhlProperty/adjustments/nonResidentLandlord")

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukNonFhlProperty/adjustments/nonResidentLandlord")))
      }

      "passed a body with ukNonFhlProperty adjustments with an empty rentARoom object" in {
        setupMocks()
        val invalidBody: JsValue = validBody.replaceWithEmptyObject("/ukNonFhlProperty/adjustments/rentARoom/jointlyLet")

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukNonFhlProperty/adjustments/rentARoom/jointlyLet")))
      }

      "passed a body with an invalid structuredBuildingAllowance/qualifyingDate" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))(
              enhancedStructuredBuildingAllowanceEntry)
          )
        validBody.update("/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate", JsString("2020.10.01"))

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateFormatError.withPath("/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid enhancedStructuredBuildingAllowance/qualifyingDate" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry.update("/firstYear/qualifyingDate", JsString("2020.10.01")))
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateFormatError.withPath("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingDate")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/name" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/name", JsString("*")))
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, StringFormatError.withPath("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/name")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(enhancedStructuredBuildingAllowanceEntry.update("/building/number", JsString("")))
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            StringFormatError.withPath("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/number")))
      }

      "passed a body with an invalid structuredBuildingAllowance/building/postcode" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry.update("/building/postcode", JsString("*")))
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            StringFormatError.withPath("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building/postcode")))
      }

      "passed a body with invalid numeric fields" should {
        def testValueFormatError(path: String): Unit = s"for $path" in {
          setupMocks()
          val result =
            validator(validNino, validBusinessId, validTaxYear, validBody.update(path, JsNumber(123.456))).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        List(
          "/ukFhlProperty/allowances/annualInvestmentAllowance",
          "/ukFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukFhlProperty/allowances/otherCapitalAllowance",
          "/ukFhlProperty/allowances/electricChargePointAllowance",
          "/ukFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukFhlProperty/adjustments/privateUseAdjustment",
          "/ukFhlProperty/adjustments/balancingCharge",
          "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges",
          "/ukNonFhlProperty/allowances/annualInvestmentAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
          "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance",
          "/ukNonFhlProperty/allowances/otherCapitalAllowance",
          "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods",
          "/ukNonFhlProperty/allowances/electricChargePointAllowance",
          "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance",
          "/ukNonFhlProperty/adjustments/balancingCharge",
          "/ukNonFhlProperty/adjustments/privateUseAdjustment",
          "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
        ).foreach(p => testValueFormatError(p))
      }

      "passed a body with invalid structuredBuildingAllowance fields" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(
              structuredBuildingAllowanceEntry
                .update("/amount", JsNumber(234.345342))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(3434.453423))
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/amount"
            ))
          ))
      }

      "passed a body with invalid enhancedStructuredBuildingAllowance fields" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .update("/amount", JsNumber(234.4576))
                .update("/firstYear/qualifyingAmountExpenditure", JsNumber(453.3424)))
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure",
              "/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/amount"
            ))
          ))
      }

      "passed a body with an invalid ukFhlProperty propertyIncomeAllowance" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukFhlProperty/allowances")
            .update("/ukFhlProperty/allowances/propertyIncomeAllowance", JsNumber(123.455))

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukFhlProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with an invalid ukNonFhlProperty propertyIncomeAllowance" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukNonFhlProperty/allowances")
            .update("/ukNonFhlProperty/allowances/propertyIncomeAllowance", JsNumber(345.676))

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukNonFhlProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with ukFhlProperty propertyIncomeAllowance that is too big" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukFhlProperty/allowances")
            .update("/ukFhlProperty/allowances/propertyIncomeAllowance", JsNumber(1000.01))

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukFhlProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with ukNonFhlProperty propertyIncomeAllowance that is too big" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .removeProperty("/ukNonFhlProperty/allowances")
            .update("/ukNonFhlProperty/allowances/propertyIncomeAllowance", JsNumber(1000.01))

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/ukNonFhlProperty/allowances/propertyIncomeAllowance", "0", "1000.0")
          ))
      }

      "passed a body with both allowances and propertyIncomeAllowance supplied for fhl" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .update("/ukFhlProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukFhlProperty/adjustments/privateUseAdjustment")

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/ukFhlProperty/allowances")))
      }

      "passed a body with both allowances and propertyIncomeAllowance supplied for non-fhl" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody
            .update("/ukNonFhlProperty/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/ukNonFhlProperty/adjustments/privateUseAdjustment")

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/ukNonFhlProperty/allowances")))
      }

      "passed a both with structuredBuildingAllowance/building with no name or number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(
              structuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )(enhancedStructuredBuildingAllowanceEntry)
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/ukNonFhlProperty/allowances/structuredBuildingAllowance/0/building")))
      }

      "passed a both with enhancedStructuredBuildingAllowance/building with no name or number" in {
        setupMocks()
        val invalidBody: JsValue =
          validBody.update(
            "/ukNonFhlProperty",
            ukNonFhlPropertyJson(structuredBuildingAllowanceEntry)(
              enhancedStructuredBuildingAllowanceEntry
                .removeProperty("/building/name")
                .removeProperty("/building/number")
            )
          )

        val result =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleBuildingNameNumberError.withPath("/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/0/building")))
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
