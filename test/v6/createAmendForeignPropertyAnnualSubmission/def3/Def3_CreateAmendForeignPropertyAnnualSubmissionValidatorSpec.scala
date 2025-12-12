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

package v6.createAmendForeignPropertyAnnualSubmission.def3

import common.models.errors.*
import play.api.libs.json.*
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty.*
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.{
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData,
  Def3_Fixtures,
  Def3_StructuredBuildingAllowanceFixture
}
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

class Def3_CreateAmendForeignPropertyAnnualSubmissionValidatorSpec
    extends UnitSpec
    with JsonErrorValidators
    with Def3_Fixtures
    with Def3_StructuredBuildingAllowanceFixture {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2026-27"

  private def entryWith(structuredBuildingAllowance: JsValue*) = Json.parse(
    s"""
      |{
      |  "propertyId": "5e8b8450-dc1b-4360-8109-7067337b42cb",
      |  "adjustments": {
      |    "privateUseAdjustment": 1.25,
      |    "balancingCharge": 2.25
      |  },
      |  "allowances": {
      |    "annualInvestmentAllowance": 1.25,
      |    "costOfReplacingDomesticItems": 2.25,
      |    "otherCapitalAllowance": 4.25,
      |    "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowance)},
      |    "zeroEmissionsCarAllowance": 6.25
      |  }
      |}
    """.stripMargin
  )

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""
      |{
      |  "foreignProperty": ${JsArray(nonFhlEntries)}
      |}
    """.stripMargin
  )

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedBodyWithUpdatedBuilding: Def3_Create_Amend_Building => Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    (building: Def3_Create_Amend_Building) =>
      def3_createAmendForeignPropertyAnnualSubmissionRequestBody.copy(
        foreignProperty = List(
          def3_foreignEntry.copy(
            propertyId = "5e8b8450-dc1b-4360-8109-7067337b42cb",
            allowances = Some(
              def3_foreignAllowances.copy(
                structuredBuildingAllowance = Some(List(def3_structuredBuildingAllowance.copy(building = building)))
              ))
          ))
      )

  private val parsedBodyWithoutBuildingNumber = parsedBodyWithUpdatedBuilding(def3_building.copy(number = None))

  private val parsedBodyWithoutBuildingName = parsedBodyWithUpdatedBuilding(def3_building.copy(name = None))

  private def validator(nino: String, businessId: String, body: JsValue) =
    new Def3_CreateAmendForeignPropertyAnnualSubmissionValidator(nino, businessId, validTaxYear, body)

  def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
    s"for $expectedPath" in {

      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, body).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
    }

  def testRuleIncorrectOrEmptyBodyWith(body: JsValue, expectedPath: String): Unit = testWith(RuleIncorrectOrEmptyBodyError)(body, expectedPath)

  def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

  def testStringFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(StringFormatError)(body, expectedPath)

  def testDateFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(DateFormatError)(body, expectedPath)

  def testForPropertyIncomeAllowance(body: JsValue, expectedPath: String): Unit =
    testWith(ValueFormatError.forPathAndRange(expectedPath, "0", "1000.0"))(body, expectedPath)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, def3_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            def3_createAmendForeignPropertyAnnualSubmissionRequestBody))
      }

      "passed a valid request with propertyIncomeAllowance" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, bodyWith(def3_propertyIncomeAllowanceRequestBodyMtdJson)).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            def3_foreignEntryPropertyIncomeAllowanceRequestBody))
      }

      "passed a valid request with minimal foreign property" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            Json.parse("""{
                         |  "foreignProperty": [
                         |    {
                         |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
                         |      "adjustments": {
                         |        "balancingCharge": 12.34
                         |      }
                         |    }
                         |  ]
                         |}
                         |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            def3_minimalForeignPropertyRequestBody))
      }

      "passed a valid request with minimal foreign property including only allowances" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            Json.parse("""{
                         |  "foreignProperty": [
                         |    {
                         |      "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
                         |      "allowances": {
                         |        "annualInvestmentAllowance": 38330.95
                         |      }
                         |    }
                         |  ]
                         |}
                         |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            def3_minimalForeignOnlyAllowancesRequestBody))
      }

      "passed a valid request where a postcode is with a name but not a number" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            bodyWith(
              entryWith(def3_structuredBuildingAllowanceMtdJson
                .removeProperty("/building/number")))).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingNumber))
      }

      "passed a valid request where a postcode is with a number but not a name" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            bodyWith(
              entryWith(def3_structuredBuildingAllowanceMtdJson
                .removeProperty("/building/name")))
          ).validateAndWrapResult()

        result shouldBe Right(
          Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingName))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, def3_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", def3_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty request body" in {
        val emptyBody: JsValue = Json.parse("""{}""")
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, emptyBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a request body with an empty or missing field" when {

        List(
          (bodyWith(), "/foreignProperty"),
          (bodyWith(def3_foreignEntryMtdJson.removeProperty("/propertyId")), "/foreignProperty/0/propertyId"),
          (bodyWith(def3_foreignEntryMtdJson.removeProperty("/adjustments").removeProperty("/allowances")), "/foreignProperty/0"),
          (bodyWith(def3_foreignEntryMtdJson.replaceWithEmptyObject("/adjustments")), "/foreignProperty/0/adjustments"),
          (bodyWith(def3_foreignEntryMtdJson.replaceWithEmptyObject("/allowances")), "/foreignProperty/0/allowances"),
          (bodyWith(entryWith()), "/foreignProperty/0/allowances/structuredBuildingAllowance"),
          (
            bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.removeProperty("/amount"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/amount"
          ),
          (
            bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.removeProperty("/firstYear/qualifyingDate"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
          ),
          (
            bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.removeProperty("/firstYear/qualifyingAmountExpenditure"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          ),
          (
            bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.removeProperty("/building/"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/building"
          ),
          (
            bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.removeProperty("/building/postcode"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/building/postcode"
          )
        ).foreach(testRuleIncorrectOrEmptyBodyWith.tupled)
      }

      "passed a request body with empty fields except for additional (non-schema) properties" in {
        val invalidBody = Json.parse("""{
                                       |  "foreignProperty": [
                                       |    {
                                       |      "unknownField": 999.99
                                       |    }
                                       |  ]
                                       |}
                                       |""".stripMargin)
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/propertyId")))
      }

      "passed a request body with a field containing an invalid value" when {
        val badValue = JsNumber(123.456)

        "adjustment or allowances (except propertyIncomeAllowance) is invalid" when {

          List(
            (
              bodyWith(def3_foreignEntryMtdJson.update("/adjustments/privateUseAdjustment", badValue)),
              "/foreignProperty/0/adjustments/privateUseAdjustment"),
            (bodyWith(def3_foreignEntryMtdJson.update("/adjustments/balancingCharge", badValue)), "/foreignProperty/0/adjustments/balancingCharge"),
            (
              bodyWith(def3_foreignEntryMtdJson.update("/allowances/annualInvestmentAllowance", badValue)),
              "/foreignProperty/0/allowances/annualInvestmentAllowance"),
            (
              bodyWith(def3_foreignEntryMtdJson.update("/allowances/costOfReplacingDomesticItems", badValue)),
              "/foreignProperty/0/allowances/costOfReplacingDomesticItems"),
            (
              bodyWith(def3_foreignEntryMtdJson.update("/allowances/otherCapitalAllowance", badValue)),
              "/foreignProperty/0/allowances/otherCapitalAllowance"),
            (
              bodyWith(def3_foreignEntryMtdJson.update("/allowances/zeroEmissionsCarAllowance", badValue)),
              "/foreignProperty/0/allowances/zeroEmissionsCarAllowance"),
            (
              bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.update("/amount", badValue))),
              "/foreignProperty/0/allowances/structuredBuildingAllowance/0/amount"),
            (
              bodyWith(entryWith(def3_structuredBuildingAllowanceMtdJson.update("/firstYear/qualifyingAmountExpenditure", badValue))),
              "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure")
          ).foreach(testValueFormatErrorWith.tupled)
        }

        "propertyIncomeAllowance allowances is invalid" when {
          List(
            (
              bodyWith(def3_propertyIncomeAllowanceRequestBodyMtdJson.update("/allowances/propertyIncomeAllowance", badValue)),
              "/foreignProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => testForPropertyIncomeAllowance.tupled(p))
        }

        "propertyIncomeAllowance allowances is too large" when {
          val bigValue = JsNumber(1000.01)
          List(
            (
              bodyWith(def3_propertyIncomeAllowanceRequestBodyMtdJson.update("/allowances/propertyIncomeAllowance", bigValue)),
              "/foreignProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => testForPropertyIncomeAllowance.tupled(p))
        }
      }
      "passed a request body with multiple fields containing invalid values" in {
        val badValue = JsNumber(123.456)
        val path1    = "/foreignProperty/0/adjustments/privateUseAdjustment"
        val path2    = "/foreignProperty/1/allowances/costOfReplacingDomesticItems"

        val invalidBody = bodyWith(
          entryWith(def3_structuredBuildingAllowanceMtdJson).update("/adjustments/privateUseAdjustment", badValue),
          def3_foreignEntryMtdJson.update("/allowances/costOfReplacingDomesticItems", badValue)
        )

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path1, path2))))
      }

      "passed a request body with a field containing an invalid string format" when {
        val badStringValue = JsString("x" * 91)

        List(
          (
            bodyWith(def3_foreignEntryMtdJson, entryWith(def3_structuredBuildingAllowanceMtdJson.update("/building/postcode", badStringValue))),
            "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/postcode"),
          (
            bodyWith(def3_foreignEntryMtdJson, entryWith(def3_structuredBuildingAllowanceMtdJson.update("/building/number", badStringValue))),
            "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/number"),
          (
            bodyWith(def3_foreignEntryMtdJson, entryWith(def3_structuredBuildingAllowanceMtdJson.update("/building/name", badStringValue))),
            "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/name")
        ).foreach(p => testStringFormatErrorWith.tupled(p))
      }

      "passed a request body with a field containing an invalid date format" when {
        val invalidBody =
          bodyWith(
            def3_foreignEntryMtdJson,
            entryWith(def3_structuredBuildingAllowanceMtdJson.update("/firstYear/qualifyingDate", JsString("9999-99-99"))))

        testDateFormatErrorWith(
          invalidBody,
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
        )
      }

      "passed a request body with a qualifyingDate before 1900" when {
        val invalidBody =
          bodyWith(
            def3_foreignEntryMtdJson,
            entryWith(def3_structuredBuildingAllowanceMtdJson.update("/firstYear/qualifyingDate", JsString("1899-01-01"))))

        testDateFormatErrorWith(
          invalidBody,
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
        )
      }

      "passed a request body with a qualifyingDate after 2100" when {
        val invalidBody =
          bodyWith(
            def3_foreignEntryMtdJson,
            entryWith(def3_structuredBuildingAllowanceMtdJson.update("/firstYear/qualifyingDate", JsString("2100-01-01"))))

        testDateFormatErrorWith(
          invalidBody,
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
        )
      }

      "passed a request body with an invalid propertyId" in {
        val invalidBody = bodyWith(def3_foreignEntryMtdJson.update("/propertyId", JsString("1234")))
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyIdFormatError))
      }

      "passed a request body with duplicated property IDs" in {
        val invalidBody = bodyWith(def3_foreignEntryMtdJson, def3_foreignEntryMtdJson)

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDuplicatePropertyIdError.forDuplicatedIdsAndPaths(
              id = "8e8b8450-dc1b-4360-8109-7067337b42cb",
              paths = List("/foreignProperty/0/propertyId", "/foreignProperty/1/propertyId")
            )
          )
        )
      }

      "passed a request body with propertyIncomeAllowance and separate allowances for foreign Property" in {
        val invalidBody = bodyWith(
          def3_foreignEntryMtdJson
            .update("/allowances/propertyIncomeAllowance", JsNumber(123.45))
            .removeProperty("/adjustments/privateUseAdjustment"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/foreignProperty/0/allowances")))
      }

      val buildingAllowanceWithoutNameOrNumber =
        def3_structuredBuildingAllowanceMtdJson
          .removeProperty("/building/name")
          .removeProperty("/building/number")

      val invalidBodyWithoutNameOrNumber = bodyWith(entryWith(buildingAllowanceWithoutNameOrNumber))
      val invalidBodyWithoutNameOrNumberMultiple =
        bodyWith(entryWith(buildingAllowanceWithoutNameOrNumber, buildingAllowanceWithoutNameOrNumber))

      "passed a request body where only the postcode is supplied in structuredBuildingAllowance" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBodyWithoutNameOrNumber).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/foreignProperty/0/allowances/structuredBuildingAllowance/0/building")))
      }

      "passed a request body where only the postcode is supplied for multiple buildings" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, invalidBodyWithoutNameOrNumberMultiple).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleBuildingNameNumberError.withPaths(
              List(
                "/foreignProperty/0/allowances/structuredBuildingAllowance/0/building",
                "/foreignProperty/0/allowances/structuredBuildingAllowance/1/building"
              ))
          ))
      }
    }
  }

}
