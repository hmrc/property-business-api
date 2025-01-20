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

package v6.createAmendForeignPropertyAnnualSubmission.def2

import common.models.errors.{RuleBothAllowancesSuppliedError, RuleBuildingNameNumberError}
import play.api.libs.json._
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty._
import v6.createAmendForeignPropertyAnnualSubmission.def2.model.request.{
  Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData
}
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

class Def2_CreateAmendForeignPropertyAnnualSubmissionValidatorSpec extends UnitSpec with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"

  private val validStructuredBuildingAllowance =
    Json.parse("""{
                 |  "amount": 3545.12,
                 |  "firstYear": {
                 |    "qualifyingDate": "2020-03-29",
                 |    "qualifyingAmountExpenditure": 3453.34
                 |  },
                 |  "building": {
                 |    "name": "Building name",
                 |    "number": "12",
                 |    "postcode": "TF3 4GH"
                 |  }
                 |}""".stripMargin)

  private def entryWith(countryCode: String, structuredBuildingAllowance: JsValue*) =
    Json.parse(s"""
                  |    {
                  |      "countryCode": "$countryCode",
                  |      "adjustments": {
                  |        "privateUseAdjustment": 4553.34,
                  |        "balancingCharge": 3453.34
                  |      },
                  |      "allowances": {
                  |        "annualInvestmentAllowance": 38330.95,
                  |        "costOfReplacingDomesticItems": 41985.17,
                  |        "zeroEmissionsGoodsVehicleAllowance": 9769.19,
                  |        "otherCapitalAllowance": 1049.21,
                  |        "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowance)},
                  |        "zeroEmissionsCarAllowance": 3456.34
                  |      }
                  |    }""".stripMargin)

  private def entryWithCountryCode(countryCode: String) = entryWith(countryCode, validStructuredBuildingAllowance)

  private val entry = entryWithCountryCode(countryCode = "AFG")

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBody = bodyWith(entry)

  private val entryPropertyIncomeAllowance = Json.parse("""
                                                          |    {
                                                          |      "countryCode": "LBN",
                                                          |      "adjustments": {
                                                          |        "balancingCharge": 3453.34
                                                          |      },
                                                          |      "allowances": {
                                                          |        "propertyIncomeAllowance": 100.95
                                                          |      }
                                                          |    }
                                                          |""".stripMargin)

  private def propertyIncomeAllowanceBodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBodyWithPropertyIncomeAllowance = propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedForeignAdjustments = Def2_Create_Amend_ForeignAdjustments(
    Some(4553.34),
    Some(3453.34)
  )

  private val parsedFirstYear = Def2_Create_Amend_FirstYear("2020-03-29", 3453.34)

  private val parsedBuilding = Def2_Create_Amend_Building(
    Some("Building name"),
    Some("12"),
    "TF3 4GH"
  )

  private val parsedStructuredBuildingAllowance = Def2_Create_Amend_StructuredBuildingAllowance(
    3545.12,
    Some(parsedFirstYear),
    parsedBuilding
  )

  private val parsedForeignAllowances = Def2_Create_Amend_ForeignAllowances(
    annualInvestmentAllowance = Some(38330.95),
    costOfReplacingDomesticItems = Some(41985.17),
    zeroEmissionsGoodsVehicleAllowance = Some(9769.19),
    otherCapitalAllowance = Some(1049.21),
    zeroEmissionsCarAllowance = Some(3456.34),
    propertyIncomeAllowance = None,
    structuredBuildingAllowance = Some(List(parsedStructuredBuildingAllowance))
  )

  private val parsedForeignEntry = Def2_Create_Amend_ForeignEntry(
    "AFG",
    Some(parsedForeignAdjustments),
    Some(parsedForeignAllowances)
  )

  private val parsedBody = Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    List(parsedForeignEntry)
  )

  private val parsedForeignEntryPropertyIncomeAllowance = Def2_Create_Amend_ForeignEntry(
    "LBN",
    Some(parsedForeignAdjustments.copy(privateUseAdjustment = None)),
    Some(
      Def2_Create_Amend_ForeignAllowances(
        None,
        None,
        None,
        None,
        None,
        Some(100.95),
        None
      ))
  )

  private val parsedBodyWithPropertyIncomeAllowance = Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    List(parsedForeignEntryPropertyIncomeAllowance)
  )

  private val parsedBodyWithMinimalforeignProperty = Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    List(
      Def2_Create_Amend_ForeignEntry(
        "LBN",
        Some(
          Def2_Create_Amend_ForeignAdjustments(
            None,
            Some(12.34)
          )),
        None
      )
    )
  )

  private val parsedBodyWithMinimalForeignOnlyAllowances = Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    List(
      Def2_Create_Amend_ForeignEntry(
        "LBN",
        None,
        Some(
          Def2_Create_Amend_ForeignAllowances(
            Some(38330.95),
            None,
            None,
            None,
            None,
            None,
            None
          )
        )
      )
    )
  )

  private val parsedBodyWithUpdatedBuilding: Def2_Create_Amend_Building => Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    (building: Def2_Create_Amend_Building) =>
      parsedBody.copy(foreignProperty = List(
        parsedForeignEntry.copy(allowances =
          Some(parsedForeignAllowances.copy(structuredBuildingAllowance = Some(List(parsedStructuredBuildingAllowance.copy(building = building))))))))

  private val parsedBodyWithoutBuildingNumber = parsedBodyWithUpdatedBuilding(parsedBuilding.copy(number = None))

  private val parsedBodyWithoutBuildingName = parsedBodyWithUpdatedBuilding(parsedBuilding.copy(name = None))

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def2_CreateAmendForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)

  def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
    s"for $expectedPath" in {

      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, body).validateAndWrapResult()

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
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with propertyIncomeAllowance" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyWithPropertyIncomeAllowance).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedBodyWithPropertyIncomeAllowance))
      }

      "passed a valid request with minimal foreign property" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
                         |  "foreignProperty": [
                         |    {
                         |      "countryCode": "LBN",
                         |      "adjustments": {
                         |        "balancingCharge": 12.34
                         |      }
                         |    }
                         |  ]
                         |}
                         |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedBodyWithMinimalforeignProperty))
      }

      "passed a valid request with minimal foreign property including only allowances" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
                         |  "foreignProperty": [
                         |    {
                         |      "countryCode": "LBN",
                         |      "allowances": {
                         |        "annualInvestmentAllowance": 38330.95
                         |      }
                         |    }
                         |  ]
                         |}
                         |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedBodyWithMinimalForeignOnlyAllowances))
      }

      "passed a valid request where a postcode is with a name but not a number" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            bodyWith(
              entryWith(
                "AFG",
                validStructuredBuildingAllowance
                  .removeProperty("/building/number")))).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingNumber))
      }

      "passed a valid request where a postcode is with a number but not a name" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            bodyWith(
              entryWith(
                "AFG",
                validStructuredBuildingAllowance
                  .removeProperty("/building/name")))
          ).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingName))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed a taxYear spanning an invalid tax year range" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2020-22", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty request body" in {
        val emptyBody: JsValue = Json.parse("""{}""")
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, emptyBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a request body with an empty or missing field" when {

        List(
          (bodyWith(), "/foreignProperty"),
          (bodyWith(entry.removeProperty("/countryCode")), "/foreignProperty/0/countryCode"),
          (bodyWith(entry.removeProperty("/adjustments").removeProperty("/allowances")), "/foreignProperty/0"),
          (bodyWith(entry.replaceWithEmptyObject("/adjustments")), "/foreignProperty/0/adjustments"),
          (bodyWith(entry.replaceWithEmptyObject("/allowances")), "/foreignProperty/0/allowances"),
          (bodyWith(entryWith("AFG")), "/foreignProperty/0/allowances/structuredBuildingAllowance"),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/amount"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/amount"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/firstYear/qualifyingDate"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/firstYear/qualifyingAmountExpenditure"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/building/"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/building"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/building/postcode"))),
            "/foreignProperty/0/allowances/structuredBuildingAllowance/0/building/postcode"
          )
        ).foreach((testRuleIncorrectOrEmptyBodyWith _).tupled)
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
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/countryCode")))
      }

      "passed a request body with a field containing an invalid value" when {
        val badValue = JsNumber(123.456)

        "adjustment or allowances (except propertyIncomeAllowance) is invalid" when {

          List(
            (bodyWith(entry.update("/adjustments/privateUseAdjustment", badValue)), "/foreignProperty/0/adjustments/privateUseAdjustment"),
            (bodyWith(entry.update("/adjustments/balancingCharge", badValue)), "/foreignProperty/0/adjustments/balancingCharge"),
            (bodyWith(entry.update("/allowances/annualInvestmentAllowance", badValue)), "/foreignProperty/0/allowances/annualInvestmentAllowance"),
            (
              bodyWith(entry.update("/allowances/costOfReplacingDomesticItems", badValue)),
              "/foreignProperty/0/allowances/costOfReplacingDomesticItems"),
            (
              bodyWith(entry.update("/allowances/zeroEmissionsGoodsVehicleAllowance", badValue)),
              "/foreignProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance"),
            (bodyWith(entry.update("/allowances/otherCapitalAllowance", badValue)), "/foreignProperty/0/allowances/otherCapitalAllowance"),
            (bodyWith(entry.update("/allowances/zeroEmissionsCarAllowance", badValue)), "/foreignProperty/0/allowances/zeroEmissionsCarAllowance"),
            (
              bodyWith(entryWith("AFG", validStructuredBuildingAllowance.update("/amount", badValue))),
              "/foreignProperty/0/allowances/structuredBuildingAllowance/0/amount"),
            (
              bodyWith(entryWith("AFG", validStructuredBuildingAllowance.update("/firstYear/qualifyingAmountExpenditure", badValue))),
              "/foreignProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure")
          ).foreach((testValueFormatErrorWith _).tupled)
        }

        "propertyIncomeAllowance allowances is invalid" when {
          List(
            (
              propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", badValue)),
              "/foreignProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }

        "propertyIncomeAllowance allowances is too large" when {
          val bigValue = JsNumber(1000.01)
          List(
            (
              propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", bigValue)),
              "/foreignProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }
      }
      "passed a request body with multiple fields containing invalid values" in {
        val badValue = JsNumber(123.456)
        val path1    = "/foreignProperty/0/adjustments/privateUseAdjustment"
        val path2    = "/foreignProperty/1/allowances/costOfReplacingDomesticItems"

        val invalidBody = bodyWith(
          entryWith(countryCode = "ZWE", validStructuredBuildingAllowance).update("/adjustments/privateUseAdjustment", badValue),
          entry.update("/allowances/costOfReplacingDomesticItems", badValue)
        )

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path1, path2))))
      }
    }

    "passed a request body with a field containing an invalid string format" when {
      val badStringValue = JsString("x" * 91)

      List(
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/postcode", badStringValue))),
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/postcode"),
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/number", badStringValue))),
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/number"),
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/name", badStringValue))),
          "/foreignProperty/1/allowances/structuredBuildingAllowance/0/building/name")
      ).foreach(p => (testStringFormatErrorWith _).tupled(p))
    }

    "passed a request body with a field containing an invalid date format" when {
      val invalidBody =
        bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/firstYear/qualifyingDate", JsString("9999-99-99"))))

      testDateFormatErrorWith(
        invalidBody,
        "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
      )
    }

    "passed a request body with a qualifyingDate before 1900" when {
      val invalidBody =
        bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/firstYear/qualifyingDate", JsString("1899-01-01"))))

      testDateFormatErrorWith(
        invalidBody,
        "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
      )
    }

    "passed a request body with a qualifyingDate after 2100" when {
      val invalidBody =
        bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/firstYear/qualifyingDate", JsString("2100-01-01"))))

      testDateFormatErrorWith(
        invalidBody,
        "/foreignProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
      )
    }

    "passed a request body with an invalid country code" in {
      val invalidBody = bodyWith(entryWithCountryCode("QQQ"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPath("/foreignProperty/0/countryCode")))
    }

    "passed a request body with multiple invalid country codes" in {
      val invalidBody = bodyWith(entryWithCountryCode("QQQ"), entryWithCountryCode("AAA"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(correlationId, RuleCountryCodeError.withPaths(List("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))))
    }

    "passed a request body with an invalid country code format" in {
      val invalidBody = bodyWith(entryWithCountryCode("XXXX"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignProperty/0/countryCode")))
    }

    "passed a request body with propertyIncomeAllowance and separate allowances for foreign Property" in {
      val invalidBody = bodyWith(
        entry
          .update("/allowances/propertyIncomeAllowance", JsNumber(123.45))
          .removeProperty("/adjustments/privateUseAdjustment"))

      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/foreignProperty/0/allowances")))
    }

    val buildingAllowanceWithoutNameOrNumber =
      validStructuredBuildingAllowance
        .removeProperty("/building/name")
        .removeProperty("/building/number")

    val invalidBodyWithoutNameOrNumber = bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber))
    val invalidBodyWithoutNameOrNumberMultiple =
      bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber, buildingAllowanceWithoutNameOrNumber))

    "passed a request body where only the postcode is supplied in structuredBuildingAllowance" in {
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBodyWithoutNameOrNumber).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(correlationId, RuleBuildingNameNumberError.withPath("/foreignProperty/0/allowances/structuredBuildingAllowance/0/building")))
    }

    "passed a request body where only the postcode is supplied for multiple buildings" in {
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBodyWithoutNameOrNumberMultiple).validateAndWrapResult()

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

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", "invalid", validBody).validateAndWrapResult()

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
