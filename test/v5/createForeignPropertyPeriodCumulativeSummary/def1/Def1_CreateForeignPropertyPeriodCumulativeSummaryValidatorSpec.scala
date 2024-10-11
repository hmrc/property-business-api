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

package v5.createForeignPropertyPeriodCumulativeSummary.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request._
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestData

class Def1_CreateForeignPropertyPeriodCumulativeSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"

  private val validFromDate    = "2025-03-29"
  private val validToDate      = "2026-03-29"
  private val validCountryCode = "AFG"

  private def entryWith(countryCode: String) = Json.parse(s"""
       |{
       |         "countryCode": "$countryCode",
       |         "income":{
       |            "rentIncome":{
       |               "rentAmount":381.21
       |            },
       |            "foreignTaxCreditRelief":true,
       |            "premiumsOfLeaseGrant":884.72,
       |            "otherPropertyIncome":7713.09,
       |            "foreignTaxPaidOrDeducted":884.12,
       |            "specialWithholdingTaxOrUkTaxPaid":847.72
       |         },
       |         "expenses":{
       |            "premisesRunningCosts":129.35,
       |            "repairsAndMaintenance":7490.32,
       |            "financialCosts":5000.99,
       |            "professionalFees":847.90,
       |            "travelCosts":69.20,
       |            "costOfServices":478.23,
       |            "residentialFinancialCost":879.28,
       |            "broughtFwdResidentialFinancialCost":846.13,
       |            "other":138.92
       |         }
       |}
       |""".stripMargin)

  private val entry = entryWith(countryCode = validCountryCode)

  private def bodyWith(Entries: JsValue*) = Json.parse(s"""
       |{
       |   "fromDate":"$validFromDate",
       |   "toDate":"$validToDate",
       |   "foreignProperty": ${JsArray(Entries)}
       |}
       |""".stripMargin)

  private val validBody = bodyWith(entry)

  private def entryConsolidated = Json.parse(s"""
       |{
       |     "countryCode":"$validCountryCode",
       |     "income":{
       |        "rentIncome":{
       |           "rentAmount":381.21
       |        },
       |        "foreignTaxCreditRelief":true,
       |        "premiumsOfLeaseGrant":884.72,
       |        "otherPropertyIncome":7713.09,
       |        "foreignTaxPaidOrDeducted":884.12,
       |        "specialWithholdingTaxOrUkTaxPaid":847.72
       |     },
       |     "expenses":{
       |        "consolidatedExpenses":129.35
       |     }
       |}
       |""".stripMargin)

  private def consolidatedBodyWith(Entries: JsValue*) = Json.parse(s"""
       |{
       |   "fromDate": "$validFromDate",
       |   "toDate": "$validToDate",
       |   "foreignProperty": ${JsArray(Entries)}
       |}
       |""".stripMargin)

  private val validBodyConsolidated = consolidatedBodyWith(entryConsolidated)

  private val validBodyMinimal = Json.parse(s"""
       |{
       |  "fromDate": "$validFromDate",
       |  "toDate": "$validToDate",
       |  "foreignProperty": [
       |    {
       |      "countryCode": "$validCountryCode",
       |      "income": {
       |        "rentIncome": {
       |          "rentAmount": 381.21
       |        }
       |      }
       |    }
       |  ]
       |}
       |""".stripMargin)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedForeignPropertyRentIncome = RentIncome(Some(381.21))

  private val parsedForeignPropertyIncome = PropertyIncome(
    Some(parsedForeignPropertyRentIncome),
    foreignTaxCreditRelief = Some(true),
    Some(884.72),
    Some(7713.09),
    Some(884.12),
    Some(847.72)
  )

  private val parsedForeignPropertyIncomeMinimal = PropertyIncome(
    Some(parsedForeignPropertyRentIncome),
    None,
    None,
    None,
    None,
    None
  )

  private val parsedCreateForeignPropertyExpenses = Expenses(
    Some(129.35),
    Some(7490.32),
    Some(5000.99),
    Some(847.90),
    Some(478.23),
    Some(69.20),
    Some(879.28),
    Some(846.13),
    Some(138.92),
    None
  )

  private val parsedCreateForeignPropertyExpensesConsolidated = Expenses(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    Some(129.35)
  )

  private val parsedForeignPropertyEntry = ForeignProperty(
    countryCode = validCountryCode,
    income = Some(parsedForeignPropertyIncome),
    expenses = Some(parsedCreateForeignPropertyExpenses)
  )

  private val parsedForeignPropertyEntryConsolidated =
    parsedForeignPropertyEntry.copy(expenses = Some(parsedCreateForeignPropertyExpensesConsolidated))

  private val parsedBody = Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody(
    fromDate = validFromDate,
    toDate = validToDate,
    foreignProperty = Some(List(parsedForeignPropertyEntry))
  )

  private val parsedBodyConsolidated = parsedBody.copy(foreignProperty = Some(List(parsedForeignPropertyEntryConsolidated)))

  private val parsedBodyMinimalForeign =
    parsedBody.copy(
      foreignProperty = Some(List(parsedForeignPropertyEntry.copy(income = Some(parsedForeignPropertyIncomeMinimal), expenses = None)))
    )

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateForeignPropertyPeriodCumulativeSummaryValidator(nino, businessId, taxYear, body, mockAppConfig)

  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV3Foreign.returns(TaxYear.starting(2025)).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with consolidated expenses" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyConsolidated))
      }

      "passed a valid request with minimal foreignProperty" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyMinimal).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalForeign))
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val taxYearString = "2025-26"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(
            Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid business id" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an invalidly formatted tax year" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        setupMocks()
        validator(validNino, validBusinessId, "2024-25", validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a tax year with an invalid range" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, "2025-27", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a body with an empty foreignProperty entry" in {
        setupMocks()
        val invalidBody = bodyWith(JsObject.empty)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/countryCode")))
      }

      "passed a body with a foreignProperty entry containing an empty expenses object" in {
        setupMocks()
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/expenses"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/expenses")))
      }

      "passed a body with a foreignProperty entry containing an empty income object" in {
        setupMocks()
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/income")))
      }

      "passed a body with a foreignProperty entry missing income and expenses" in {
        setupMocks()
        val invalidBody = bodyWith(entry.removeProperty("/income").removeProperty("/expenses"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0")))
      }

      "passed a body with a foreignProperty entry containing an empty income/rentIncome object" in {
        setupMocks()
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income/rentIncome"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/income/rentIncome")))
      }

      "passed a body with an invalidly formatted fromDate" in {
        setupMocks()
        val invalidBody = validBody.update("/fromDate", JsString("invalid"))
        print(invalidBody)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a fromDate out of range" in {
        setupMocks()
        val invalidBody = validBody.update("/fromDate", JsString("1782-09-04"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in {
        setupMocks()
        val invalidBody = validBody.update("/toDate", JsString("invalid"))

        print(invalidBody)
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate out of range" in {
        setupMocks()
        val invalidBody = validBody.update("/toDate", JsString("3054-03-29"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body where the toDate precedes fromDate" in {
        setupMocks()
        val invalidBody = validBody
          .update("/fromDate", JsString("2021-01-01"))
          .update("/toDate", JsString("2020-01-01"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a duplicate country code" in {
        setupMocks()
        val invalidBody = bodyWith(entry, entry)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(code = validCountryCode, paths = List("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))
          ))
      }

      "passed a body with multiple duplicate country codes" in {
        setupMocks()
        val countryCode1 = "AFG"
        val countryCode2 = "ZWE"
        val invalidBody  = bodyWith(entryWith(countryCode1), entryWith(countryCode2), entryWith(countryCode1), entryWith(countryCode2))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(code = countryCode1, paths = List("/foreignProperty/0/countryCode", "/foreignProperty/2/countryCode")),
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(code = countryCode2, paths = List("/foreignProperty/1/countryCode", "/foreignProperty/3/countryCode"))
            ))
          ))
      }

      def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
        s"for $expectedPath" in {
          setupMocks()
          val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
        }

      def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

      "passed a body with an entry containing an invalid amount" when {
        val badValue = JsNumber(42.768)
        List(
          "/income/rentIncome/rentAmount",
          "/income/premiumsOfLeaseGrant",
          "/income/otherPropertyIncome",
          "/income/foreignTaxPaidOrDeducted",
          "/income/specialWithholdingTaxOrUkTaxPaid",
          "/expenses/premisesRunningCosts",
          "/expenses/repairsAndMaintenance",
          "/expenses/financialCosts",
          "/expenses/professionalFees",
          "/expenses/costOfServices",
          "/expenses/travelCosts",
          "/expenses/other",
          "/expenses/residentialFinancialCost",
          "/expenses/broughtFwdResidentialFinancialCost"
        ).foreach(path => testValueFormatErrorWith(bodyWith(entry.update(path, badValue)), s"/foreignProperty/0$path"))

        testValueFormatErrorWith(
          bodyWith(entryConsolidated.update("/expenses/consolidatedExpenses", badValue)),
          s"/foreignProperty/0/expenses/consolidatedExpenses")
      }

      "passed a body with multiple fields containing invalid amounts" in {
        setupMocks()
        val badValue = JsNumber(42.768)
        val invalidBody = bodyWith(
          entry
            .update("/income/premiumsOfLeaseGrant", badValue)
            .update("/expenses/financialCosts", badValue)
            .update("/expenses/travelCosts", badValue)
        )

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List(
                "/foreignProperty/0/income/premiumsOfLeaseGrant",
                "/foreignProperty/0/expenses/financialCosts",
                "/foreignProperty/0/expenses/travelCosts"
              ))
          ))
      }

      "passed a body containing an invalid country code" in {
        setupMocks()
        val invalidBody = bodyWith(entryWith("JUY"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPath("/foreignProperty/0/countryCode")))
      }

      "passed a body containing a multiple invalid country codes" in {
        setupMocks()
        val invalidBody = bodyWith(entryWith("ABC"), entryWith("DEF"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleCountryCodeError.withPaths(List("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))))
      }

      "passed a body containing an invalidly formatted country code" in {
        setupMocks()
        val invalidBody = bodyWith(entryWith("12345678"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignProperty/0/countryCode")))

      }

      "passed a body containing a multiple invalidly formatted country codes" in {
        setupMocks()
        val invalidBody = bodyWith(entryWith("12345678"), entryWith("34567890"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, CountryCodeFormatError.withPaths(List("/foreignProperty/0/countryCode", "/foreignProperty/1/countryCode"))))
      }

      "passed a body containing both foreignProperty expenses" in {
        setupMocks()
        val invalidBody = bodyWith(entry.update("/expenses/consolidatedExpenses", JsNumber(100.00)))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignProperty/0/expenses")))
      }

      "passed a body containing multiple sub-objects with both expenses" in {
        setupMocks()
        val entryWithBothExpenses0 = entryWith("AFG").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val entryWithBothExpenses1 = entryWith("ZWE").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val invalidBody =
          bodyWith(entryWithBothExpenses0, entryWithBothExpenses1).update("/foreignFhlEea/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleBothExpensesSuppliedError.withPaths(
              List(
                "/foreignProperty/0/expenses",
                "/foreignProperty/1/expenses"
              ))
          ))

      }

    }

    "return multiple errors" when {
      "the path parameters have multiple issues" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
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
