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
import config.MockAppConfig
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString, JsValue, Json}
import support.UnitSpec
import v2.models.request.createForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.request.createForeignPropertyPeriodSummary.foreignPropertyEntry._
import v2.models.request.createForeignPropertyPeriodSummary.{
  CreateForeignPropertyPeriodSummaryRequestBody,
  CreateForeignPropertyPeriodSummaryRequestData
}

class CreateForeignPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val validFromDate    = "2020-03-29"
  private val validToDate      = "2021-03-29"
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

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(s"""
         |{
         |   "fromDate":"$validFromDate",
         |   "toDate":"$validToDate",
         |   "foreignFhlEea":{
         |      "income":{
         |         "rentAmount":381.21
         |      },
         |      "expenses":{
         |         "premisesRunningCosts":993.31,
         |         "repairsAndMaintenance":8842.23,
         |         "financialCosts":994,
         |         "professionalFees":992.12,
         |         "costOfServices":4620.23,
         |         "travelCosts":774,
         |         "other":984.41
         |      }
         |   },
         |   "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
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

  private def consolidatedBodyWith(nonFhlEntries: JsValue*) = Json.parse(s"""
       |{
       |   "fromDate": "$validFromDate",
       |   "toDate": "$validToDate",
       |   "foreignFhlEea":{
       |      "income":{
       |         "rentAmount":381.21
       |      },
       |      "expenses":{
       |         "consolidatedExpenses":993.31
       |      }
       |   },
       |   "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin)

  private val validBodyConsolidated = consolidatedBodyWith(entryConsolidated)

  private val validBodyMinimalFhl = Json.parse(s"""
       |{
       |  "fromDate": "$validFromDate",
       |  "toDate": "$validToDate",
       |  "foreignFhlEea": {
       |    "income": {
       |    "rentAmount": 381.21
       |    }
       |  }
       |}
       |""".stripMargin)

  private val validBodyMinimalNonFhl = Json.parse(s"""
        |{
        |  "fromDate": "$validFromDate",
        |  "toDate": "$validToDate",
        |  "foreignNonFhlProperty": [
        |    {
        |      "countryCode": "$validCountryCode",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 381.21
        |        },
        |        "foreignTaxCreditRelief": true
        |      }
        |    }
        |  ]
        |}
        |""".stripMargin)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedForeignFhlEeaIncome = ForeignFhlEeaIncome(Some(381.21))

  // @formatter:off
  private val parsedCreateForeignFhlEeaExpenses = CreateForeignFhlEeaExpenses(
    Some(993.31), Some(8842.23), Some(994), Some(992.12),
    Some(4620.23), Some(774), Some(984.41), None
  )

  private val parsedCreateForeignFhlEeaExpensesConsolidated = CreateForeignFhlEeaExpenses(
    None,None,None,None,None,None, None,Some(993.31)
  )
  // @formatter:on

  private val parsedForeignFhlEea = CreateForeignFhlEea(
    Some(parsedForeignFhlEeaIncome),
    Some(parsedCreateForeignFhlEeaExpenses)
  )

  private val parsedForeignFhlEeaConsolidated = CreateForeignFhlEea(
    Some(parsedForeignFhlEeaIncome),
    Some(parsedCreateForeignFhlEeaExpensesConsolidated)
  )

  private val parsedForeignNonFhlPropertyRentIncome = ForeignNonFhlPropertyRentIncome(Some(381.21))

  // @formatter:off
  private val parsedForeignNonFhlPropertyIncome = ForeignNonFhlPropertyIncome(
    Some(parsedForeignNonFhlPropertyRentIncome), foreignTaxCreditRelief = true,
    Some(884.72), Some(7713.09), Some(884.12), Some(847.72)
  )

  private val parsedForeignNonFhlPropertyIncomeMinimal = ForeignNonFhlPropertyIncome(
    Some(parsedForeignNonFhlPropertyRentIncome), foreignTaxCreditRelief = true,
    None,None, None, None
  )

  private val parsedCreateForeignNonFhlPropertyExpenses = CreateForeignNonFhlPropertyExpenses(
    Some(129.35), Some(7490.32), Some(5000.99), Some(847.90), Some(478.23),
    Some(69.20), Some(879.28), Some(846.13), Some(138.92), None
  )

  private val parsedCreateForeignNonFhlPropertyExpensesConsolidated = CreateForeignNonFhlPropertyExpenses(
    None, None, None, None, None, None, None, None, None, Some(129.35)
  )
  // @formatter:on

  private val parsedForeignNonFhlPropertyEntry = CreateForeignNonFhlPropertyEntry(
    countryCode = validCountryCode,
    income = Some(parsedForeignNonFhlPropertyIncome),
    expenses = Some(parsedCreateForeignNonFhlPropertyExpenses)
  )

  private val parsedForeignNonFhlPropertyEntryConsolidated =
    parsedForeignNonFhlPropertyEntry.copy(expenses = Some(parsedCreateForeignNonFhlPropertyExpensesConsolidated))

  private val parsedBody = CreateForeignPropertyPeriodSummaryRequestBody(
    fromDate = validFromDate,
    toDate = validToDate,
    foreignFhlEea = Some(parsedForeignFhlEea),
    foreignNonFhlProperty = Some(List(parsedForeignNonFhlPropertyEntry))
  )

  private val parsedBodyConsolidated = parsedBody.copy(
    foreignFhlEea = Some(parsedForeignFhlEeaConsolidated),
    foreignNonFhlProperty = Some(List(parsedForeignNonFhlPropertyEntryConsolidated)))

  private val parsedBodyMinimalForeignFhl =
    parsedBody.copy(foreignFhlEea = Some(parsedForeignFhlEea.copy(expenses = None)), foreignNonFhlProperty = None)

  private val parsedBodyMinimalForeignNonFhl =
    parsedBody.copy(
      foreignFhlEea = None,
      foreignNonFhlProperty =
        Some(List(parsedForeignNonFhlPropertyEntry.copy(income = Some(parsedForeignNonFhlPropertyIncomeMinimal), expenses = None)))
    )

  private val validatorFactory = new CreateForeignPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    validatorFactory.validator(nino, businessId, taxYear, body)

  MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021))

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(CreateForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with consolidated expenses" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(CreateForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyConsolidated))
      }

      "passed a valid request with minimal foreignFhlEea" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyMinimalFhl).validateAndWrapResult()

        result shouldBe Right(CreateForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalForeignFhl))
      }

      "passed a valid request with minimal foreignNonFhlProperty" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyMinimalNonFhl).validateAndWrapResult()

        result shouldBe Right(
          CreateForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalForeignNonFhl))
      }

      "passed the minimum supported taxYear" in {
        val taxYearString = "2021-22"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(CreateForeignPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid business id" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in {
        validator(validNino, validBusinessId, "2020-21", validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a tax year with an invalid range" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2021-23", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a body with an empty foreignFhlEea" in {
        val invalidBody = validBody.replaceWithEmptyObject("/foreignFhlEea")

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignFhlEea")))
      }

      "passed a body with an empty foreignFhlEea/expenses" in {
        val invalidBody = validBody.replaceWithEmptyObject("/foreignFhlEea/expenses")

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignFhlEea/expenses")))
      }

      "passed a body with an empty foreignFhlEea/income" in {
        val invalidBody = validBody.replaceWithEmptyObject("/foreignFhlEea/income")

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignFhlEea/income")))
      }

      "passed a body with an empty foreignNonFhlProperty entry" in {
        val invalidBody = bodyWith(JsObject.empty)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignNonFhlProperty/0/countryCode")))
      }

      "passed a body with a foreignNonFhlProperty entry containing an empty expenses object" in {
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/expenses"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignNonFhlProperty/0/expenses")))
      }

      "passed a body with a foreignNonFhlProperty entry containing an empty income object" in {
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignNonFhlProperty/0/income/foreignTaxCreditRelief")))
      }

      "passed a body with a foreignNonFhlProperty entry missing income and expenses" in {
        val invalidBody = bodyWith(entry.removeProperty("/income").removeProperty("/expenses"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignNonFhlProperty/0")))
      }

      "passed a body with a foreignNonFhlProperty entry containing an empty income/rentIncome object" in {
        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income/rentIncome"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignNonFhlProperty/0/income/rentIncome")))
      }

      "passed a body with an invalidly formatted fromDate" in {
        val invalidBody = validBody.update("/fromDate", JsString("invalid"))
        print(invalidBody)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a fromDate out of range" in {
        val invalidBody = validBody.update("/fromDate", JsString("1782-09-04"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in {
        val invalidBody = validBody.update("/toDate", JsString("invalid"))

        print(invalidBody)
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate out of range" in {
        val invalidBody = validBody.update("/toDate", JsString("3054-03-29"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body where the toDate precedes fromDate" in {
        val invalidBody = validBody
          .update("/fromDate", JsString("2021-01-01"))
          .update("/toDate", JsString("2020-01-01"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a duplicate country code" in {
        val invalidBody = bodyWith(entry, entry)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDuplicateCountryCodeError
              .forDuplicatedCodesAndPaths(
                code = validCountryCode,
                paths = List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))
          ))
      }

      "passed a body with multiple duplicate country codes" in {
        val countryCode1 = "AFG"
        val countryCode2 = "ZWE"
        val invalidBody  = bodyWith(entryWith(countryCode1), entryWith(countryCode2), entryWith(countryCode1), entryWith(countryCode2))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(
                  code = countryCode1,
                  paths = List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/2/countryCode")),
              RuleDuplicateCountryCodeError
                .forDuplicatedCodesAndPaths(
                  code = countryCode2,
                  paths = List("/foreignNonFhlProperty/1/countryCode", "/foreignNonFhlProperty/3/countryCode"))
            ))
          ))
      }

      def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
        s"for $expectedPath" in {
          val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
        }

      def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

      "passed a body with an invalid amount" when {
        val badValue = JsNumber(42.768)
        List(
          "/foreignFhlEea/income/rentAmount",
          "/foreignFhlEea/expenses/premisesRunningCosts",
          "/foreignFhlEea/expenses/repairsAndMaintenance",
          "/foreignFhlEea/expenses/financialCosts",
          "/foreignFhlEea/expenses/professionalFees",
          "/foreignFhlEea/expenses/costOfServices",
          "/foreignFhlEea/expenses/travelCosts",
          "/foreignFhlEea/expenses/other"
        ).foreach(path => testValueFormatErrorWith(validBody.update(path, badValue), path))

        testValueFormatErrorWith(
          validBodyConsolidated.update("/foreignFhlEea/expenses/consolidatedExpenses", badValue),
          "/foreignFhlEea/expenses/consolidatedExpenses")
      }

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
        ).foreach(path => testValueFormatErrorWith(bodyWith(entry.update(path, badValue)), s"/foreignNonFhlProperty/0$path"))

        testValueFormatErrorWith(
          bodyWith(entryConsolidated.update("/expenses/consolidatedExpenses", badValue)),
          s"/foreignNonFhlProperty/0/expenses/consolidatedExpenses")
      }

      "passed a body with multiple fields containing invalid amounts" in {
        val badValue = JsNumber(42.768)
        val invalidBody = bodyWith(
          entry
            .update("/income/premiumsOfLeaseGrant", badValue)
            .update("/expenses/financialCosts", badValue)
            .update("/expenses/travelCosts", badValue)
        ).update("/foreignFhlEea/income/rentAmount", badValue)

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/foreignFhlEea/income/rentAmount",
              "/foreignNonFhlProperty/0/income/premiumsOfLeaseGrant",
              "/foreignNonFhlProperty/0/expenses/financialCosts",
              "/foreignNonFhlProperty/0/expenses/travelCosts"
            ))
          ))
      }

      "passed a body containing an invalid country code" in {
        val invalidBody = bodyWith(entryWith("JUY"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPath("/foreignNonFhlProperty/0/countryCode")))
      }

      "passed a body containing a multiple invalid country codes" in {
        val invalidBody = bodyWith(entryWith("ABC"), entryWith("DEF"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleCountryCodeError.withPaths(List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }

      "passed a body containing an invalidly formatted country code" in {
        val invalidBody = bodyWith(entryWith("12345678"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignNonFhlProperty/0/countryCode")))

      }

      "passed a body containing a multiple invalidly formatted country codes" in {
        val invalidBody = bodyWith(entryWith("12345678"), entryWith("34567890"))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            CountryCodeFormatError.withPaths(List("/foreignNonFhlProperty/0/countryCode", "/foreignNonFhlProperty/1/countryCode"))))
      }

      "passed a body containing both foreignFhlEea expenses" in {
        val invalidBody = validBody.update("/foreignFhlEea/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses")))
      }

      "passed a body containing both foreignNonFhlProperty expenses" in {
        val invalidBody = bodyWith(entry.update("/expenses/consolidatedExpenses", JsNumber(100.00)))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignNonFhlProperty/0/expenses")))
      }

      "passed a body containing multiple sub-objects with both expenses" in {
        val entryWithBothExpenses0 = entryWith("AFG").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val entryWithBothExpenses1 = entryWith("ZWE").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val invalidBody =
          bodyWith(entryWithBothExpenses0, entryWithBothExpenses1).update("/foreignFhlEea/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleBothExpensesSuppliedError.withPaths(
              List(
                "/foreignFhlEea/expenses",
                "/foreignNonFhlProperty/0/expenses",
                "/foreignNonFhlProperty/1/expenses"
              ))
          ))

      }

    }

    "return multiple errors" when {
      "the path parameters have multiple issues" in {
        val result: Either[ErrorWrapper, CreateForeignPropertyPeriodSummaryRequestData] =
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
