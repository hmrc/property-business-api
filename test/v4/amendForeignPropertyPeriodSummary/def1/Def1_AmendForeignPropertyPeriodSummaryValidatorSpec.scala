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

package v4.amendForeignPropertyPeriodSummary.def1

import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsArray, JsNumber, JsValue, Json}
import support.UnitSpec
import v4.amendForeignPropertyPeriodSummary.def1.model.request.foreignFhlEea._
import v4.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry._
import v4.amendForeignPropertyPeriodSummary.model.request.{AmendForeignPropertyPeriodSummaryRequestData, Def1_AmendForeignPropertyPeriodSummaryRequestBody, Def1_AmendForeignPropertyPeriodSummaryRequestData}

class Def1_AmendForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear = "2023-24"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val countryCode = "AFG"

  private def nonFhlEntryWith(countryCode: String) = Json.parse(
    s"""
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

  private val entryConsolidated = Json.parse(
    """
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

  private val validBodyConsolidatedExpenses = consolidatedBodyWith(entryConsolidated)

  private val validBodyMinimalFhl = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |       "rentAmount": 1123.89
      |    }
      |  }
      |}
      |""".stripMargin)

  private val validBodyMinimalNonFhl = Json.parse(
    """
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

  private val parsedNino = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val incomeFhl = ForeignFhlEeaIncome(
    rentAmount = Some(1123.89)
  )

  private val expensesFhl = AmendForeignFhlEeaExpenses(
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
  private val expensesFhlConsolidated = AmendForeignFhlEeaExpenses(
    None, None, None, None, None, None, None,
    consolidatedExpenses = Some(332.78)
  )
  //@formatter:on

  private val foreignFhlEea = AmendForeignFhlEea(
    income = Some(incomeFhl),
    expenses = Some(expensesFhl)
  )

  private val foreignFhlEeaConsolidated = AmendForeignFhlEea(
    income = Some(incomeFhl),
    expenses = Some(expensesFhlConsolidated)
  )

  private val foreignNonFhlPropertyRentIncome = ForeignNonFhlPropertyRentIncome(
    rentAmount = Some(440.31)
  )

  private val incomeNonFhl = ForeignNonFhlPropertyIncome(
    rentIncome = Some(foreignNonFhlPropertyRentIncome),
    foreignTaxCreditRelief = false,
    premiumsOfLeaseGrant = Some(950.48),
    otherPropertyIncome = Some(802.49),
    foreignTaxPaidOrDeducted = Some(734.18),
    specialWithholdingTaxOrUkTaxPaid = Some(85.47)
  )

  private val incomeNonFhlMinimal = ForeignNonFhlPropertyIncome(
    foreignTaxCreditRelief = true,
    rentIncome = Some(foreignNonFhlPropertyRentIncome),
    premiumsOfLeaseGrant = None,
    otherPropertyIncome = None,
    foreignTaxPaidOrDeducted = None,
    specialWithholdingTaxOrUkTaxPaid = None
  )

  private val expensesNonFhl = AmendForeignNonFhlPropertyExpenses(
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
  private val expensesNonFhlConsolidated = AmendForeignNonFhlPropertyExpenses(
    None, None, None, None, None, None, None, None, None,
    consolidatedExpenses = Some(332.78)
  )
  //@formatter:on

  private def foreignNonFhlProperty = AmendForeignNonFhlPropertyEntry(
    countryCode = countryCode,
    income = Some(incomeNonFhl),
    expenses = Some(expensesNonFhl)
  )

  private def parsedBody: Def1_AmendForeignPropertyPeriodSummaryRequestBody =
    Def1_AmendForeignPropertyPeriodSummaryRequestBody(Some(foreignFhlEea), Some(List(foreignNonFhlProperty)))

  private val parsedBodyConsolidatedExpenses = Def1_AmendForeignPropertyPeriodSummaryRequestBody(
    Some(foreignFhlEeaConsolidated),
    Some(List(foreignNonFhlProperty.copy(expenses = Some(expensesNonFhlConsolidated))))
  )

  private val parsedBodyMinimalFhl = Def1_AmendForeignPropertyPeriodSummaryRequestBody(
    Some(foreignFhlEea.copy(expenses = None)),
    None
  )

  private val parsedBodyMinimalNonFhl = Def1_AmendForeignPropertyPeriodSummaryRequestBody(
    None,
    Some(List(foreignNonFhlProperty.copy(income = Some(incomeNonFhlMinimal), expenses = None)))
  )

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue) =
    new Def1_AmendForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId, body, mockAppConfig)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Right(
          Def1_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBody))
      }

      "passed a valid request with consolidated expenses" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyConsolidatedExpenses).validateAndWrapResult()

        result shouldBe Right(
          Def1_AmendForeignPropertyPeriodSummaryRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            parsedSubmissionId,
            parsedBodyConsolidatedExpenses))
      }

      "passed a valid request with minimal fhl" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyMinimalFhl).validateAndWrapResult()

        result shouldBe Right(
          Def1_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBodyMinimalFhl))
      }

      "passed a valid request with minimal non-fhl" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyMinimalNonFhl).validateAndWrapResult()

        result shouldBe Right(
          Def1_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedSubmissionId, parsedBodyMinimalNonFhl))
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2021-22"
        validator(validNino, validBusinessId, taxYearString, validSubmissionId, validBody).validateAndWrapResult() shouldBe
          Right(Def1_AmendForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedSubmissionId, parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted businessId" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid business id", validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "202324", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        setupMocks()
        validator(validNino, validBusinessId, "2020-21", validSubmissionId, validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2019-21", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an incorrectly formatted submissionId" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }

      "passed an empty request body" in {
        setupMocks()
        val emptyBody: JsValue = Json.parse("""{}""")
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, emptyBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
        s"for $expectedPath" in {
          setupMocks()
          val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, validSubmissionId, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
        }

      def testRuleIncorrectOrEmptyBodyWith(body: JsValue, expectedPath: String): Unit = testWith(RuleIncorrectOrEmptyBodyError)(body, expectedPath)

      def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

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
        List(
          "/foreignFhlEea/income/rentAmount",
          "/foreignFhlEea/expenses/premisesRunningCosts",
          "/foreignFhlEea/expenses/repairsAndMaintenance",
          "/foreignFhlEea/expenses/financialCosts",
          "/foreignFhlEea/expenses/professionalFees",
          "/foreignFhlEea/expenses/costOfServices",
          "/foreignFhlEea/expenses/other",
          "/foreignFhlEea/expenses/travelCosts"
        ).foreach(path => testValueFormatErrorWith(validBody.update(path, badValue), path))

        List(
          (bodyWith(entry.update("/income/rentIncome/rentAmount", badValue)), "/foreignNonFhlProperty/0/income/rentIncome/rentAmount"),
          (bodyWith(entry.update("/income/premiumsOfLeaseGrant", badValue)), "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant"),
          (bodyWith(entry.update("/income/otherPropertyIncome", badValue)), "/foreignNonFhlProperty/0/income/otherPropertyIncome"),
          (bodyWith(entry.update("/income/foreignTaxPaidOrDeducted", badValue)), "/foreignNonFhlProperty/0/income/foreignTaxPaidOrDeducted"),
          (
            bodyWith(entry.update("/income/specialWithholdingTaxOrUkTaxPaid", badValue)),
            "/foreignNonFhlProperty/0/income/specialWithholdingTaxOrUkTaxPaid"),
          (bodyWith(entry.update("/expenses/premisesRunningCosts", badValue)), "/foreignNonFhlProperty/0/expenses/premisesRunningCosts"),
          (bodyWith(entry.update("/expenses/repairsAndMaintenance", badValue)), "/foreignNonFhlProperty/0/expenses/repairsAndMaintenance"),
          (bodyWith(entry.update("/expenses/financialCosts", badValue)), "/foreignNonFhlProperty/0/expenses/financialCosts"),
          (bodyWith(entry.update("/expenses/professionalFees", badValue)), "/foreignNonFhlProperty/0/expenses/professionalFees"),
          (bodyWith(entry.update("/expenses/travelCosts", badValue)), "/foreignNonFhlProperty/0/expenses/travelCosts"),
          (bodyWith(entry.update("/expenses/costOfServices", badValue)), "/foreignNonFhlProperty/0/expenses/costOfServices"),
          (bodyWith(entry.update("/expenses/residentialFinancialCost", badValue)), "/foreignNonFhlProperty/0/expenses/residentialFinancialCost"),
          (
            bodyWith(entry.update("/expenses/broughtFwdResidentialFinancialCost", badValue)),
            "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost"),
          (bodyWith(entry.update("/expenses/other", badValue)), "/foreignNonFhlProperty/0/expenses/other")
        ).foreach((testValueFormatErrorWith _).tupled)
      }

      "passed a request body with invalid consolidated expenses" when {
        List(
          "/foreignFhlEea/expenses/consolidatedExpenses"
        ).foreach(path => testValueFormatErrorWith(validBodyConsolidatedExpenses.update(path, badValue), path))

        List(
          (
            consolidatedBodyWith(entryConsolidated.update("/expenses/consolidatedExpenses", badValue)),
            "/foreignNonFhlProperty/0/expenses/consolidatedExpenses"),
          (
            consolidatedBodyWith(entryConsolidated.update("/expenses/residentialFinancialCost", badValue)),
            "/foreignNonFhlProperty/0/expenses/residentialFinancialCost"),
          (
            consolidatedBodyWith(entryConsolidated.update("/expenses/broughtFwdResidentialFinancialCost", badValue)),
            "/foreignNonFhlProperty/0/expenses/broughtFwdResidentialFinancialCost")
        ).foreach(p => (testValueFormatErrorWith _).tupled(p))
      }

      "passed a request body with multiple invalid fields" in {
        setupMocks()
        val path0 = "/foreignFhlEea/expenses/travelCosts"
        val path1 = "/foreignNonFhlProperty/0/expenses/travelCosts"
        val path2 = "/foreignNonFhlProperty/1/income/rentIncome/rentAmount"

        val bodyWithMultipleInvalidFields: JsValue =
          bodyWith(
            nonFhlEntryWith(countryCode = "ZWE").update("/expenses/travelCosts", badValue),
            entry.update("/income/rentIncome/rentAmount", badValue))
            .update(path0, badValue)

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithMultipleInvalidFields).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path0, path1, path2))))
      }

      "passed a request body with an invalid country code" in {
        setupMocks()
        val bodyWithInvalidCountryCode: JsValue = bodyWith(nonFhlEntryWith(countryCode = "QQQ"), nonFhlEntryWith(countryCode = "AAA"))

        val path0 = "/foreignNonFhlProperty/0/countryCode"
        val path1 = "/foreignNonFhlProperty/1/countryCode"

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, bodyWithInvalidCountryCode).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPaths(List(path0, path1))))
      }

      "passed a request body with duplicated country codes" in {
        setupMocks()
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

      "passed a request body with multiple duplicated country codes" in {
        setupMocks()
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

      "passed a request body with consolidated and separate expenses for fhl" in {
        setupMocks()
        val invalidBody: JsValue = validBody.update("foreignFhlEea/expenses/consolidatedExpenses", JsNumber(123.45))

        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses")))
      }

      "passed a request body with consolidated and separate expenses for non-fhl" in {
        setupMocks()
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
      "the request has multiple issues (path parameters)" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendForeignPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", "invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError, SubmissionIdFormatError, BusinessIdFormatError))
          )
        )
      }
    }
  }

}
