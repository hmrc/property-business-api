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

package v2.controllers.validators

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsArray, JsNumber, JsString, JsValue, Json}
import support.UnitSpec
import v2.models.request.common.{Building, FirstYear, StructuredBuildingAllowance}
import v2.models.request.createAmendForeignPropertyAnnualSubmission._
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances}
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl.{
  ForeignNonFhlAdjustments,
  ForeignNonFhlAllowances,
  ForeignNonFhlEntry
}

class CreateAmendForeignPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2021-22"

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
                  |        "electricChargePointAllowance": 3565.45,
                  |        "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowance)},
                  |        "zeroEmissionsCarAllowance": 3456.34
                  |      }
                  |    }""".stripMargin)

  private def entryWithCountryCode(countryCode: String) = entryWith(countryCode, validStructuredBuildingAllowance)

  private val entry = entryWithCountryCode(countryCode = "AFG")

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "adjustments": {
       |      "privateUseAdjustment": 34343.45,
       |      "balancingCharge": 53543.23,
       |      "periodOfGraceAdjustment": true
       |    },
       |    "allowances": {
       |      "annualInvestmentAllowance": 3434.23,
       |      "otherCapitalAllowance": 1343.34,
       |      "electricChargePointAllowance": 6565.45,
       |      "zeroEmissionsCarAllowance": 3456.34
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
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
       |  "foreignFhlEea": {
       |    "adjustments": {
       |      "periodOfGraceAdjustment": true
       |    },
       |    "allowances": {
       |      "propertyIncomeAllowance": 100.95
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBodyWithPropertyIncomeAllowance = propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedForeignFhlEeaAdjustments = ForeignFhlEeaAdjustments(
    Some(34343.45),
    Some(53543.23),
    periodOfGraceAdjustment = true
  )

  private val parsedForeignFhlEeaAllowances = ForeignFhlEeaAllowances(
    Some(3434.23),
    Some(1343.34),
    Some(6565.45),
    Some(3456.34),
    None
  )

  private val parsedForeignFhlEea = ForeignFhlEea(
    Some(parsedForeignFhlEeaAdjustments),
    Some(parsedForeignFhlEeaAllowances)
  )

  private val parsedForeignNonFhlAdjustments = ForeignNonFhlAdjustments(
    Some(4553.34),
    Some(3453.34)
  )

  private val parsedFirstYear = FirstYear("2020-03-29", 3453.34)

  private val parsedBuilding = Building(
    Some("Building name"),
    Some("12"),
    "TF3 4GH"
  )

  private val parsedStructuredBuildingAllowance = StructuredBuildingAllowance(
    3545.12,
    Some(parsedFirstYear),
    parsedBuilding
  )

  private val parsedForeignNonFhlAllowances = ForeignNonFhlAllowances(
    annualInvestmentAllowance = Some(38330.95),
    costOfReplacingDomesticItems = Some(41985.17),
    zeroEmissionsGoodsVehicleAllowance = Some(9769.19),
    otherCapitalAllowance = Some(1049.21),
    electricChargePointAllowance = Some(3565.45),
    zeroEmissionsCarAllowance = Some(3456.34),
    propertyIncomeAllowance = None,
    structuredBuildingAllowance = Some(List(parsedStructuredBuildingAllowance))
  )

  private val parsedForeignNonFhlEntry = ForeignNonFhlEntry(
    "AFG",
    Some(parsedForeignNonFhlAdjustments),
    Some(parsedForeignNonFhlAllowances)
  )

  private val parsedBody = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    Some(parsedForeignFhlEea),
    Some(List(parsedForeignNonFhlEntry))
  )

  private val parsedForeignFhlEeaPropertyIncomeAllowance = ForeignFhlEea(
    Some(parsedForeignFhlEeaAdjustments.copy(privateUseAdjustment = None, balancingCharge = None)),
    Some(parsedForeignFhlEeaAllowances.copy(None, None, None, None, Some(100.95)))
  )

  private val parsedForeignNonFhlEntryPropertyIncomeAllowance = ForeignNonFhlEntry(
    "LBN",
    Some(parsedForeignNonFhlAdjustments.copy(privateUseAdjustment = None)),
    Some(
      ForeignNonFhlAllowances(
        None,
        None,
        None,
        None,
        None,
        None,
        Some(100.95),
        None
      ))
  )

  private val parsedBodyWithPropertyIncomeAllowance = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    Some(parsedForeignFhlEeaPropertyIncomeAllowance),
    Some(List(parsedForeignNonFhlEntryPropertyIncomeAllowance))
  )

  private val parsedBodyWithMinimalFhl = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    Some(
      ForeignFhlEea(
        Some(ForeignFhlEeaAdjustments(None, None, periodOfGraceAdjustment = true)),
        None
      )),
    None
  )

  private val parsedBodyWithMinimalFhlOnlyAllowances = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    Some(
      ForeignFhlEea(
        None,
        Some(
          ForeignFhlEeaAllowances(
            Some(38330.95),
            None,
            None,
            None,
            None
          ))
      )
    ),
    None)

  private val parsedBodyWithMinimalNonFhl = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    None,
    Some(
      List(
        ForeignNonFhlEntry(
          "LBN",
          Some(
            ForeignNonFhlAdjustments(
              None,
              Some(12.34)
            )),
          None
        )
      )
    )
  )

  private val parsedBodyWithMinimalNonFhlOnlyAllowances = CreateAmendForeignPropertyAnnualSubmissionRequestBody(
    None,
    Some(
      List(
        ForeignNonFhlEntry(
          "LBN",
          None,
          Some(
            ForeignNonFhlAllowances(
              Some(38330.95),
              None,
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
  )

  private val parsedBodyWithUpdatedBuilding: Building => CreateAmendForeignPropertyAnnualSubmissionRequestBody = (building: Building) =>
    parsedBody.copy(foreignNonFhlProperty = Some(
      List(parsedForeignNonFhlEntry.copy(allowances = Some(
        parsedForeignNonFhlAllowances.copy(structuredBuildingAllowance = Some(List(parsedStructuredBuildingAllowance.copy(building = building)))))))))

  private val parsedBodyWithoutBuildingNumber = parsedBodyWithUpdatedBuilding(parsedBuilding.copy(number = None))

  private val parsedBodyWithoutBuildingName = parsedBodyWithUpdatedBuilding(parsedBuilding.copy(name = None))

  private val validatorFactory = new CreateAmendForeignPropertyAnnualSubmissionValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    validatorFactory.validator(nino, businessId, taxYear, body)

  // TODO make setup method instead
  class SetUp {

    MockAppConfig.minimumTaxV2Foreign
      .returns(2021)
      .anyNumberOfTimes()

  }

  def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
    s"for $expectedPath" in new SetUp {

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
      "passed a valid request" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with propertyIncomeAllowance" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyWithPropertyIncomeAllowance).validateAndWrapResult()

        result shouldBe Right(
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithPropertyIncomeAllowance))
      }

      "passed a valid request with minimal fhl" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
               |{
               |  "foreignFhlEea": {
               |    "adjustments": {
               |      "periodOfGraceAdjustment": true
               |    }
               |  }
               |}
               |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithMinimalFhl))
      }

      "passed a valid request with minimal fhl including only allowances" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""
                 |{
                 |  "foreignFhlEea": {
                 |    "allowances": {
                 |      "annualInvestmentAllowance": 38330.95
                 |    }
                 |  }
                 |}
                 |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithMinimalFhlOnlyAllowances))
      }

      "passed a valid request with minimal non-fhl" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
                 |  "foreignNonFhlProperty": [
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
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithMinimalNonFhl))
      }

      "passed a valid request with minimal non-fhl including only allowances" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            Json.parse("""{
                 |  "foreignNonFhlProperty": [
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
          CreateAmendForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedBodyWithMinimalNonFhlOnlyAllowances))
      }

      "passed a valid request where a postcode is with a name but not a number" in new SetUp {
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
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingNumber))
      }

      "passed a valid request where a postcode is with a number but not a name" in new SetUp {
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
          CreateAmendForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyWithoutBuildingName))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "202324", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an unsupported taxYear" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2020-21", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, "2020-22", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted businessId" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty request body" in new SetUp {
        val emptyBody: JsValue = Json.parse("""{}""")
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, emptyBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a request body with an empty or missing field" when {
        List(
          "/foreignFhlEea",
          "/foreignFhlEea/allowances"
        ).foreach(path => testRuleIncorrectOrEmptyBodyWith(bodyWith(entry).replaceWithEmptyObject(path), path))

        testRuleIncorrectOrEmptyBodyWith(
          bodyWith(entry).replaceWithEmptyObject("/foreignFhlEea/adjustments"),
          "/foreignFhlEea/adjustments/periodOfGraceAdjustment")

        List(
          (bodyWith(), "/foreignNonFhlProperty"),
          (bodyWith(entry.removeProperty("/countryCode")), "/foreignNonFhlProperty/0/countryCode"),
          (bodyWith(entry.removeProperty("/adjustments").removeProperty("/allowances")), "/foreignNonFhlProperty/0"),
          (bodyWith(entry.replaceWithEmptyObject("/adjustments")), "/foreignNonFhlProperty/0/adjustments"),
          (bodyWith(entry.replaceWithEmptyObject("/allowances")), "/foreignNonFhlProperty/0/allowances"),
          (bodyWith(entryWith("AFG")), "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance"),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/amount"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/amount"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/firstYear/qualifyingDate"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/firstYear/qualifyingAmountExpenditure"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/building/"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building"
          ),
          (
            bodyWith(entryWith("AFG", validStructuredBuildingAllowance.removeProperty("/building/postcode"))),
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building/postcode"
          )
        ).foreach((testRuleIncorrectOrEmptyBodyWith _).tupled)
      }

      "passed a request body with empty fields except for additional (non-schema) properties" in new SetUp {
        private val invalidBody = Json.parse("""{
                                       |    "foreignFhlEea":{
                                       |       "unknownField": 999.99
                                       |    }
                                       |}""".stripMargin)
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignFhlEea")))
      }

      "passed a request body with a field containing an invalid value" when {
        val badValue = JsNumber(123.456)

        "adjustment or allowances (except propertyIncomeAllowance) is invalid" when {
          List(
            "/foreignFhlEea/adjustments/privateUseAdjustment",
            "/foreignFhlEea/adjustments/balancingCharge",
            "/foreignFhlEea/allowances/annualInvestmentAllowance",
            "/foreignFhlEea/allowances/otherCapitalAllowance",
            "/foreignFhlEea/allowances/electricChargePointAllowance",
            "/foreignFhlEea/allowances/zeroEmissionsCarAllowance"
          ).foreach(path => testValueFormatErrorWith(validBody.update(path, badValue), path))

          List(
            (bodyWith(entry.update("/adjustments/privateUseAdjustment", badValue)), "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment"),
            (bodyWith(entry.update("/adjustments/balancingCharge", badValue)), "/foreignNonFhlProperty/0/adjustments/balancingCharge"),
            (
              bodyWith(entry.update("/allowances/annualInvestmentAllowance", badValue)),
              "/foreignNonFhlProperty/0/allowances/annualInvestmentAllowance"),
            (
              bodyWith(entry.update("/allowances/costOfReplacingDomesticItems", badValue)),
              "/foreignNonFhlProperty/0/allowances/costOfReplacingDomesticItems"),
            (
              bodyWith(entry.update("/allowances/zeroEmissionsGoodsVehicleAllowance", badValue)),
              "/foreignNonFhlProperty/0/allowances/zeroEmissionsGoodsVehicleAllowance"),
            (bodyWith(entry.update("/allowances/otherCapitalAllowance", badValue)), "/foreignNonFhlProperty/0/allowances/otherCapitalAllowance"),
            (
              bodyWith(entry.update("/allowances/electricChargePointAllowance", badValue)),
              "/foreignNonFhlProperty/0/allowances/electricChargePointAllowance"),
            (
              bodyWith(entry.update("/allowances/zeroEmissionsCarAllowance", badValue)),
              "/foreignNonFhlProperty/0/allowances/zeroEmissionsCarAllowance"),
            (
              bodyWith(entryWith("AFG", validStructuredBuildingAllowance.update("/amount", badValue))),
              "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/amount"),
            (
              bodyWith(entryWith("AFG", validStructuredBuildingAllowance.update("/firstYear/qualifyingAmountExpenditure", badValue))),
              "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/firstYear/qualifyingAmountExpenditure")
          ).foreach((testValueFormatErrorWith _).tupled)
        }

        "propertyIncomeAllowance allowances is invalid" when {
          List(
            "/foreignFhlEea/allowances/propertyIncomeAllowance"
          ).foreach(path => testForPropertyIncomeAllowance(validBodyWithPropertyIncomeAllowance.update(path, badValue), path))

          List(
            (
              propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", badValue)),
              "/foreignNonFhlProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }

        "propertyIncomeAllowance allowances is too large" when {
          val bigValue = JsNumber(1000.01)
          List(
            "/foreignFhlEea/allowances/propertyIncomeAllowance"
          ).foreach(path => testForPropertyIncomeAllowance(validBodyWithPropertyIncomeAllowance.update(path, bigValue), path))

          List(
            (
              propertyIncomeAllowanceBodyWith(entryPropertyIncomeAllowance.update("/allowances/propertyIncomeAllowance", bigValue)),
              "/foreignNonFhlProperty/0/allowances/propertyIncomeAllowance")
          ).foreach(p => (testForPropertyIncomeAllowance _).tupled(p))
        }

        "passed a request body with empty fields except for additional (non-schema) properties" in new SetUp {
          private val path0 = "/foreignFhlEea/adjustments/privateUseAdjustment"
          private val path1 = "/foreignNonFhlProperty/0/adjustments/privateUseAdjustment"
          private val path2 = "/foreignNonFhlProperty/1/allowances/costOfReplacingDomesticItems"

          private val invalidBody = bodyWith(
            entryWith(countryCode = "ZWE", validStructuredBuildingAllowance).update("/adjustments/privateUseAdjustment", badValue),
            entry.update("/allowances/costOfReplacingDomesticItems", badValue)
          ).update(path0, badValue)

          val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
            validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path0, path1, path2))))
        }
      }
    }

    "passed a request body with a field containing an invalid string format" when {
      val badStringValue = JsString("x" * 91)

      List(
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/postcode", badStringValue))),
          "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/postcode"),
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/number", badStringValue))),
          "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/number"),
        (
          bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/building/name", badStringValue))),
          "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/building/name")
      ).foreach(p => (testStringFormatErrorWith _).tupled(p))
    }

    "passed a request body with a field containing an invalid date format" when {
      val invalidBody =
        bodyWith(entry, entryWith("ZWE", validStructuredBuildingAllowance.update("/firstYear/qualifyingDate", JsString("9999-99-99"))))

      testDateFormatErrorWith(
        invalidBody,
        "/foreignNonFhlProperty/1/allowances/structuredBuildingAllowance/0/firstYear/qualifyingDate"
      )
    }

    "passed a request body with an invalid country code" in new SetUp {
      private val invalidBody = bodyWith(entryWithCountryCode("QQQ"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPath("/foreignNonFhlProperty/0/countryCode")))
    }

    "passed a request body with multiple invalid country codes" in new SetUp {
      private val invalidBody = bodyWith(entryWithCountryCode("QQQ"), entryWithCountryCode("AAA"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          RuleCountryCodeError.withPaths(List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
    }

    "passed a request body with an invalid country code format" in new SetUp {
      private val invalidBody = bodyWith(entryWithCountryCode("XXXX"))
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignNonFhlProperty/0/countryCode")))
    }

    "passed a request body with propertyIncomeAllowance and separate allowances for fhl" in new SetUp {
      private val invalidBody = validBody
        .update("/foreignFhlEea/allowances/propertyIncomeAllowance", JsNumber(123.45))
        .removeProperty("/foreignFhlEea/adjustments/privateUseAdjustment")

      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/foreignFhlEea/allowances")))
    }

    "passed a request body with propertyIncomeAllowance and separate allowances for non-fhl" in new SetUp {
      private val invalidBody = bodyWith(
        entry
          .update("/allowances/propertyIncomeAllowance", JsNumber(123.45))
          .removeProperty("/adjustments/privateUseAdjustment"))

      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleBothAllowancesSuppliedError.withPath("/foreignNonFhlProperty/0/allowances")))
    }

    val buildingAllowanceWithoutNameOrNumber =
      validStructuredBuildingAllowance
        .removeProperty("/building/name")
        .removeProperty("/building/number")

    val invalidBodyWithoutNameOrNumber = bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber))
    val invalidBodyWithoutNameOrNumberMultiple =
      bodyWith(entryWith("AFG", buildingAllowanceWithoutNameOrNumber, buildingAllowanceWithoutNameOrNumber))

    "passed a request body where only the postcode is supplied in structuredBuildingAllowance" in new SetUp {
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBodyWithoutNameOrNumber).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          RuleBuildingNameNumberError.withPath("/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building")))
    }

    "passed a request body where only the postcode is supplied for multiple buildings" in new SetUp {
      val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
        validator(validNino, validBusinessId, validTaxYear, invalidBodyWithoutNameOrNumberMultiple).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          RuleBuildingNameNumberError.withPaths(List(
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/0/building",
            "/foreignNonFhlProperty/0/allowances/structuredBuildingAllowance/1/building"
          ))
        ))
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetUp {
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyAnnualSubmissionRequestData] =
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
