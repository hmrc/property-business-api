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

package v4.amendForeignPropertyPeriodSummary.def2

import common.models.domain.SubmissionId
import common.models.errors.{RuleBothExpensesSuppliedError, RuleDuplicateCountryCodeError, SubmissionIdFormatError}
import config.MockPropertyBusinessConfig
import play.api.libs.json.{JsArray, JsNumber, JsValue, Json}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v4.amendForeignPropertyPeriodSummary.def2.model.request.def2_foreignFhlEea._
import v4.amendForeignPropertyPeriodSummary.def2.model.request.def2_foreignPropertyEntry._
import v4.amendForeignPropertyPeriodSummary.model.request.{
  AmendForeignPropertyPeriodSummaryRequestData,
  Def2_AmendForeignPropertyPeriodSummaryRequestBody,
  Def2_AmendForeignPropertyPeriodSummaryRequestData
}

class Def2_AmendForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino           = "AA123456A"
  private val validBusinessId     = "XAIS12345678901"
  private val validTaxYear        = "2024-25"
  private val validSubmissionId   = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val maxTaxYear: TaxYear = TaxYear.fromMtd("2024-25")

  private val countryCode = "AFG"

  private def nonFhlEntryWith(countryCode: String) = Json.parse(s"""
       |{
       |  "countryCode": "$countryCode",
       |  "income": {
       |    "rentIncome": {
       |      "rentAmount": 440.31
       |    },
       |    "foreignTaxCreditRelief": false,
       |    "premiumsOfLeaseGrant": 950.48,
       |    "otherPropertyIncome": 802.49,
       |    "foreignTaxPaidOrDeducted": 734.18,
       |    "specialWithholdingTaxOrUkTaxPaid": 85.47
       |  },
       |  "expenses": {
       |    "premisesRunningCosts": 332.78,
       |    "repairsAndMaintenance": 231.45,
       |    "financialCosts": 345.23,
       |    "professionalFees": 232.45,
       |    "travelCosts": 234.67,
       |    "costOfServices": 231.56,
       |    "residentialFinancialCost": 12.34,
       |    "broughtFwdResidentialFinancialCost": 12.34,
       |    "other": 3457.9
       |  }
       |}""".stripMargin)

  private val entry = nonFhlEntryWith(countryCode = countryCode)

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 1123.89
       |    },
       |    "expenses": {
       |      "premisesRunningCosts": 332.78,
       |      "repairsAndMaintenance": 231.45,
       |      "financialCosts": 345.23,
       |      "professionalFees": 232.45,
       |      "travelCosts": 234.67,
       |      "costOfServices": 231.56,
       |      "other": 3457.9
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBody = bodyWith(entry)

  private val entryConsolidated = Json.parse("""
      |{
      |  "countryCode": "AFG",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 440.31
      |     },
      |     "foreignTaxCreditRelief": false,
      |     "premiumsOfLeaseGrant": 950.48,
      |      "otherPropertyIncome": 802.49,
      |      "foreignTaxPaidOrDeducted": 734.18,
      |      "specialWithholdingTaxOrUkTaxPaid": 85.47
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": 332.78
      |  }
      |}""".stripMargin)

  private val entryConsolidatedWithExtraFields = Json.parse("""
      |{
      |  "countryCode": "AFG",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 440.31
      |     },
      |     "foreignTaxCreditRelief": false,
      |     "premiumsOfLeaseGrant": 950.48,
      |      "otherPropertyIncome": 802.49,
      |      "foreignTaxPaidOrDeducted": 734.18,
      |      "specialWithholdingTaxOrUkTaxPaid": 85.47
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": 332.78,
      |    "residentialFinancialCost":879.28,
      |    "broughtFwdResidentialFinancialCost":846.13
      |  }
      |}""".stripMargin)

  private def consolidatedBodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 1123.89
       |    },
       |    "expenses": {
       |      "consolidatedExpenses": 332.78
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBodyConsolidatedExpenses            = consolidatedBodyWith(entryConsolidated)
  private val validBodyExtraFieldsConsolidatedExpenses = consolidatedBodyWith(entryConsolidatedWithExtraFields)

  private val validBodyMinimalFhl = Json.parse("""
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |       "rentAmount": 1123.89
      |    }
      |  }
      |}
      |""".stripMargin)

  private val validBodyMinimalNonFhl = Json.parse("""
      |{
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "foreignTaxCreditRelief": true,
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        }
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val incomeFhl = Def2_ForeignFhlEeaIncome(
    rentAmount = Some(1123.89)
  )

  private val expensesFhl = Def2_AmendForeignFhlEeaExpenses(
    premisesRunningCosts = Some(332.78),
    repairsAndMaintenance = Some(231.45),
    financialCosts = Some(345.23),
    professionalFees = Some(232.45),
    travelCosts = Some(234.67),
    costOfServices = Some(231.56),
    other = Some(3457.9),
    consolidatedExpenses = None
  )

  //@formatter:off
  private val expensesFhlConsolidated = Def2_AmendForeignFhlEeaExpenses(
    None, None, None, None, None, None, None,
    consolidatedExpenses = Some(332.78)
  )
  //@formatter:on

  private val foreignFhlEea = Def2_AmendForeignFhlEea(
    income = Some(incomeFhl),
    expenses = Some(expensesFhl)
  )

  private val foreignFhlEeaConsolidated = Def2_AmendForeignFhlEea(
    income = Some(incomeFhl),
    expenses = Some(expensesFhlConsolidated)
  )

  private val foreignNonFhlPropertyRentIncome = Def2_ForeignNonFhlPropertyRentIncome(
    rentAmount = Some(440.31)
  )

  private val incomeNonFhl = Def2_ForeignNonFhlPropertyIncome(
    rentIncome = Some(foreignNonFhlPropertyRentIncome),
    foreignTaxCreditRelief = false,
    premiumsOfLeaseGrant = Some(950.48),
    otherPropertyIncome = Some(802.49),
    foreignTaxPaidOrDeducted = Some(734.18),
    specialWithholdingTaxOrUkTaxPaid = Some(85.47)
  )

  private val incomeNonFhlMinimal = Def2_ForeignNonFhlPropertyIncome(
    foreignTaxCreditRelief = true,
    rentIncome = Some(foreignNonFhlPropertyRentIncome),
    premiumsOfLeaseGrant = None,
    otherPropertyIncome = None,
    foreignTaxPaidOrDeducted = None,
    specialWithholdingTaxOrUkTaxPaid = None
  )

  private val expensesNonFhl = Def2_AmendForeignNonFhlPropertyExpenses(
    premisesRunningCosts = Some(332.78),
    repairsAndMaintenance = Some(231.45),
    financialCosts = Some(345.23),
    professionalFees = Some(232.45),
    travelCosts = Some(234.67),
    costOfServices = Some(231.56),
    residentialFinancialCost = Some(12.34),
    broughtFwdResidentialFinancialCost = Some(12.34),
    other = Some(3457.9),
    consolidatedExpenses = None
  )

  //@formatter:off
  private val expensesNonFhlConsolidated = Def2_AmendForeignNonFhlPropertyExpenses(
    None, None, None, None, None, None, None, None, None,
    consolidatedExpenses = Some(332.78)
  )

  private val expensesNonFhlConsolidatedWithExtraFields = Def2_AmendForeignNonFhlPropertyExpenses(
    None, None, None, None, None, None, residentialFinancialCost = Some(879.28), broughtFwdResidentialFinancialCost = Some(846.13), None,
    consolidatedExpenses = Some(332.78)
  )
  //@formatter:on

  private def foreignNonFhlProperty = Def2_AmendForeignNonFhlPropertyEntry(
    countryCode = countryCode,
    income = Some(incomeNonFhl),
    expenses = Some(expensesNonFhl)
  )

  private def parsedBody: Def2_AmendForeignPropertyPeriodSummaryRequestBody =
    Def2_AmendForeignPropertyPeriodSummaryRequestBody(Some(foreignFhlEea), Some(List(foreignNonFhlProperty)))

  private val parsedBodyConsolidatedExpenses = Def2_AmendForeignPropertyPeriodSummaryRequestBody(
    Some(foreignFhlEeaConsolidated),
    Some(List(foreignNonFhlProperty.copy(expenses = Some(expensesNonFhlConsolidated))))
  )

  private val parsedBodyExtraFieldsConsolidatedExpenses = Def2_AmendForeignPropertyPeriodSummaryRequestBody(
    Some(foreignFhlEeaConsolidated),
    Some(List(foreignNonFhlProperty.copy(expenses = Some(expensesNonFhlConsolidatedWithExtraFields))))
  )

  private val parsedBodyMinimalFhl = Def2_AmendForeignPropertyPeriodSummaryRequestBody(
    Some(foreignFhlEea.copy(expenses = None)),
    None
  )

  private val parsedBodyMinimalNonFhl = Def2_AmendForeignPropertyPeriodSummaryRequestBody(
    None,
    Some(List(foreignNonFhlProperty.copy(income = Some(incomeNonFhlMinimal), expenses = None)))
  )

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue, maxTaxYear: TaxYear = maxTaxYear) =
    new Def2_AmendForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maxTaxYear, submissionId, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBody))
      }

      "passed a valid request with consolidated expenses" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyConsolidatedExpenses).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendForeignPropertyPeriodSummaryRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedSubmissionId,
            parsedBodyConsolidatedExpenses))
      }

      "passed a valid request with consolidated expenses with extra field" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyExtraFieldsConsolidatedExpenses).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendForeignPropertyPeriodSummaryRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedSubmissionId,
            parsedBodyExtraFieldsConsolidatedExpenses))
      }

      "passed a valid request with minimal fhl" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyMinimalFhl).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBodyMinimalFhl))
      }

      "passed a valid request with minimal non-fhl" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyMinimalNonFhl).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBodyMinimalNonFhl))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted businessId" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid business id", validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an incorrectly formatted taxYear" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "202324", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2020-21", validSubmissionId, validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear immediately after the maximum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2025-26", validSubmissionId, validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2019-21", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted submissionId" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }

      "passed an empty request body" in new SetupConfig {
        val emptyBody: JsValue = Json.parse("""{}""")
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, emptyBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
        s"for $expectedPath" in new SetupConfig {
          val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, validSubmissionId, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
        }

      def testRuleIncorrectOrEmptyBodyWith(body: JsValue, expectedPath: String): Unit = testWith(RuleIncorrectOrEmptyBodyError)(body, expectedPath)

      def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

      def testValueFormatErrorWithNegativeValue(body: JsValue, path: String): Unit =
        testWith(ValueFormatError.forPathAndRange(path, "-99999999999.99", "99999999999.99"))(body, path)

      val badValue = JsNumber(123.456)

      "passed a request body with an empty or missing field" when {
        List(
          "/foreignFhlEea",
          "/foreignFhlEea/income",
          "/foreignFhlEea/expenses"
        ).foreach(path => testRuleIncorrectOrEmptyBodyWith(bodyWith(entry).replaceWithEmptyObject(path), path))

        List(
          (bodyWith(), "/foreignNonFhlProperty"),
          (bodyWith(entry.replaceWithEmptyObject("/income/rentIncome")), "/foreignNonFhlProperty/0/income/rentIncome"),
          (bodyWith(entry.replaceWithEmptyObject("/expenses")), "/foreignNonFhlProperty/0/expenses"),
          (bodyWith(entry.removeProperty("/countryCode")), "/foreignNonFhlProperty/0/countryCode"),
          (bodyWith(entry.removeProperty("/income/foreignTaxCreditRelief")), "/foreignNonFhlProperty/0/income/foreignTaxCreditRelief"),
          (bodyWith(entry.removeProperty("/income").removeProperty("/expenses")), "/foreignNonFhlProperty/0")
        ).foreach((testRuleIncorrectOrEmptyBodyWith _).tupled)

      }

      "passed a request body with invalid income or (non-consolidated) expenses" when {
        val path = "/foreignFhlEea/income/rentAmount"
        testValueFormatErrorWith(validBody.update(path, badValue), path)

        List(
          "/foreignFhlEea/expenses/premisesRunningCosts",
          "/foreignFhlEea/expenses/repairsAndMaintenance",
          "/foreignFhlEea/expenses/financialCosts",
          "/foreignFhlEea/expenses/professionalFees",
          "/foreignFhlEea/expenses/costOfServices",
          "/foreignFhlEea/expenses/other",
          "/foreignFhlEea/expenses/travelCosts"
        ).foreach(path => testValueFormatErrorWithNegativeValue(validBody.update(path, badValue), path))

        List(
          (bodyWith(entry.update("/income/rentIncome/rentAmount", badValue)), "/foreignNonFhlProperty/0/income/rentIncome/rentAmount"),
          (bodyWith(entry.update("/income/premiumsOfLeaseGrant", badValue)), "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant"),
          (bodyWith(entry.update("/income/otherPropertyIncome", badValue)), "/foreignNonFhlProperty/0/income/otherPropertyIncome"),
          (bodyWith(entry.update("/income/foreignTaxPaidOrDeducted", badValue)), "/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted"),
          (
            bodyWith(entry.update("/income/specialWithholdingTaxOrUkTaxPaid", badValue)),
            "/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid"),
          (bodyWith(entry.update("/expenses/residentialFinancialCost", badValue)), "/foreignNonFhlProperty/0/expenses/residentialFinancialCost"),
          (
            bodyWith(entry.update("/expenses/broughtFwdResidentialFinancialCost", badValue)),
            "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")
        ).foreach((testValueFormatErrorWith _).tupled)

        List(
          (bodyWith(entry.update("/expenses/premisesRunningCosts", badValue)), "/foreignNonFhlProperty/0/expenses/premisesRunningCosts"),
          (bodyWith(entry.update("/expenses/repairsAndMaintenance", badValue)), "/foreignNonFhlProperty/0/expenses/repairsAndMaintenance"),
          (bodyWith(entry.update("/expenses/financialCosts", badValue)), "/foreignNonFhlProperty/0/expenses/financialCosts"),
          (bodyWith(entry.update("/expenses/professionalFees", badValue)), "/foreignNonFhlProperty/0/expenses/professionalFees"),
          (bodyWith(entry.update("/expenses/travelCosts", badValue)), "/foreignNonFhlProperty/0/expenses/travelCosts"),
          (bodyWith(entry.update("/expenses/costOfServices", badValue)), "/foreignNonFhlProperty/0/expenses/costOfServices"),
          (bodyWith(entry.update("/expenses/other", badValue)), "/foreignNonFhlProperty/0/expenses/other")
        ).foreach((testValueFormatErrorWithNegativeValue _).tupled)
      }

      "passed a request body with invalid consolidated expenses" when {
        val path = "/foreignFhlEea/expenses/consolidatedExpenses"
        testValueFormatErrorWithNegativeValue(validBodyConsolidatedExpenses.update(path, badValue), path)
      }

      "passed a request body with multiple invalid fields" in new SetupConfig {
        val path0 = "/foreignFhlEea/expenses/travelCosts"
        val path1 = "/foreignNonFhlProperty/0/expenses/repairsAndMaintenance"
        val path2 = "/foreignNonFhlProperty/0/expenses/travelCosts"

        val bodyWithMultipleInvalidFields: JsValue =
          bodyWith(
            nonFhlEntryWith(countryCode = "ZWE")
              .update("/expenses/travelCosts", badValue)
              .update("/expenses/repairsAndMaintenance", badValue))
            .update(path0, badValue)

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithMultipleInvalidFields).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange(path0, "-99999999999.99", "99999999999.99").withPaths(List(path0, path1, path2))))

      }

      "passed a request body with an invalid country code" in new SetupConfig {
        val bodyWithInvalidCountryCode: JsValue = bodyWith(nonFhlEntryWith(countryCode = "QQQ"), nonFhlEntryWith(countryCode = "AAA"))

        val path0 = "/foreignNonFhlProperty/0/countryCode"
        val path1 = "/foreignNonFhlProperty/1/countryCode"

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithInvalidCountryCode).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPaths(List(path0, path1))))
      }

      "passed a request body with duplicated country codes" in new SetupConfig {
        val countryCode = "ZWE"

        val bodyWithDuplicatedCountryCode: JsValue = bodyWith(nonFhlEntryWith(countryCode), nonFhlEntryWith(countryCode))

        val path0 = "/foreignNonFhlProperty/0/countryCode"
        val path1 = "/foreignNonFhlProperty/1/countryCode"

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithDuplicatedCountryCode).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = countryCode, paths = List(path0, path1))
          ))
      }

      "passed a request body with multiple duplicated country codes" in new SetupConfig {
        val countryCode1 = "AFG"
        val countryCode2 = "ZWE"

        val bodyWithDuplicatedCountryCodes: JsValue =
          bodyWith(nonFhlEntryWith(countryCode1), nonFhlEntryWith(countryCode2), nonFhlEntryWith(countryCode1), nonFhlEntryWith(countryCode2))

        val paths0: List[String] = List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")
        val paths1: List[String] = List("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode")

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithDuplicatedCountryCodes).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(code = countryCode1, paths = paths0),
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(code = countryCode2, paths = paths1)
            ))
          ))
      }

      "passed a request body with consolidated and separate expenses for fhl" in new SetupConfig {
        val invalidBody: JsValue = validBody.update("foreignFhlEea/expenses/consolidatedExpenses", JsNumber(123.45))

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses")))
      }

      "passed a request body with consolidated and separate expenses for non-fhl" in new SetupConfig {
        val invalidBody: JsValue = bodyWith(
          nonFhlEntryWith(countryCode = "ZWE").update("expenses/consolidatedExpenses", JsNumber(123.45)),
          entry.update("expenses/consolidatedExpenses", JsNumber(123.45))
        )

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleBothExpensesSuppliedError.withPaths(List("/foreignNonFhlProperty/0/expenses", "/foreignNonFhlProperty/1/expenses"))))

      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", "invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError, SubmissionIdFormatError, TaxYearFormatError))
          )
        )
      }
    }
  }

}
