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

package v6.createAmendForeignPropertyCumulativePeriodSummary.def2

import common.models.errors.*
import play.api.libs.json.*
import shared.models.domain.*
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.model.request.*
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

class Def2_CreateAmendForeignPropertyCumulativePeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2026-27"

  private val validFromDate   = "2026-04-06"
  private val validToDate     = "2026-07-05"
  private val validPropertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  private def entryWith(propertyId: String) = Json.parse(
    s"""
      |{
      |  "propertyId": "$propertyId",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 381.21
      |    },
      |    "foreignTaxCreditRelief": true,
      |    "premiumsOfLeaseGrant": 884.72,
      |    "otherPropertyIncome": 7713.09,
      |    "foreignTaxPaidOrDeducted": 884.12,
      |    "specialWithholdingTaxOrUkTaxPaid": 847.72
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 129.35,
      |    "repairsAndMaintenance": 7490.32,
      |    "financialCosts": 5000.99,
      |    "professionalFees": 847.90,
      |    "travelCosts": 69.20,
      |    "costOfServices": 478.23,
      |    "residentialFinancialCost": 879.28,
      |    "broughtFwdResidentialFinancialCost": 846.13,
      |    "other": 138.92
      |  }
      |}
    """.stripMargin
  )

  private val entry = entryWith(propertyId = validPropertyId)

  private def bodyWith(Entries: JsValue*) = Json.parse(
    s"""
      |{
      |  "fromDate": "$validFromDate",
      |  "toDate": "$validToDate",
      |  "foreignProperty": ${JsArray(Entries)}
      |}
    """.stripMargin
  )

  private val validBody = bodyWith(entry)

  private def emptyDatesBodyWith(Entries: JsValue*) = Json.parse(
    s"""
      |{
      |   "foreignProperty": ${JsArray(Entries)}
      |}
    """.stripMargin
  )

  private val emptyDatesBody = emptyDatesBodyWith(entry)

  private def entryConsolidated = Json.parse(s"""
       |{
       |     "propertyId":"$validPropertyId",
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
       |  "foreignProperty": [
       |    {
       |      "propertyId": "$validPropertyId",
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
    propertyId = validPropertyId,
    income = Some(parsedForeignPropertyIncome),
    expenses = Some(parsedCreateForeignPropertyExpenses)
  )

  private val parsedForeignPropertyEntryConsolidated =
    parsedForeignPropertyEntry.copy(expenses = Some(parsedCreateForeignPropertyExpensesConsolidated))

  private val parsedBody = Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody(
    fromDate = Some(validFromDate),
    toDate = Some(validToDate),
    foreignProperty = Seq(parsedForeignPropertyEntry)
  )

  private val emptyDateParsedBody = Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody(
    fromDate = None,
    toDate = None,
    foreignProperty = Seq(parsedForeignPropertyEntry)
  )

  private val parsedBodyConsolidated = parsedBody.copy(foreignProperty = Seq(parsedForeignPropertyEntryConsolidated))

  private val parsedBodyMinimalForeign =
    Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody(
      fromDate = None,
      toDate = None,
      foreignProperty = Seq(parsedForeignPropertyEntry.copy(income = Some(parsedForeignPropertyIncomeMinimal), expenses = None))
    )

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def2_CreateAmendForeignPropertyCumulativePeriodSummaryValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with consolidated expenses" in {

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyConsolidated))
      }

      "passed a valid request with minimal foreignProperty" in {

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyMinimal).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalForeign))
      }

      "passed a request with no 'from' and 'to' dates" in {
        validator(validNino, validBusinessId, validTaxYear, emptyDatesBody).validateAndWrapResult() shouldBe
          Right(
            Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(
              parsedNino,
              parsedBusinessId,
              TaxYear.fromMtd(validTaxYear),
              emptyDateParsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator("invalid nino", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid business id" in {

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a body with an empty foreignProperty entry" in {

        val invalidBody = bodyWith(JsObject.empty)

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/propertyId")))
      }

      "passed a body with a foreignProperty entry containing an empty expenses object" in {

        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/expenses"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/expenses")))
      }

      "passed a body with a foreignProperty entry containing an empty income object" in {

        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/income")))
      }

      "passed a body with a foreignProperty entry missing income and expenses" in {

        val invalidBody = bodyWith(entry.removeProperty("/income").removeProperty("/expenses"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0")))
      }

      "passed a body with a foreignProperty entry containing an empty income/rentIncome object" in {

        val invalidBody = bodyWith(entry.replaceWithEmptyObject("/income/rentIncome"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignProperty/0/income/rentIncome")))
      }

      "passed a body with an invalidly formatted fromDate" in {

        val invalidBody = validBody.update("/fromDate", JsString("invalid"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a fromDate out of range" in {

        val invalidBody = validBody.update("/fromDate", JsString("1782-09-04"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in {

        val invalidBody = validBody.update("/toDate", JsString("invalid"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate out of range" in {

        val invalidBody = validBody.update("/toDate", JsString("3054-03-29"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body where the toDate precedes fromDate" in {

        val invalidBody = validBody
          .update("/fromDate", JsString("2021-01-01"))
          .update("/toDate", JsString("2020-01-01"))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a missing fromDate" in {
        val requestWithoutFromDate = validBody.removeProperty("/fromDate")

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, requestWithoutFromDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingSubmissionDatesError))
      }

      "passed a body with a missing toDate" in {
        val requestWithoutToDate = validBody.removeProperty("/toDate")

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, requestWithoutToDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleMissingSubmissionDatesError))
      }

      def testWith(error: MtdError)(body: JsValue, expectedPath: String): Unit =
        s"for $expectedPath" in {

          val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(expectedPath)))
        }

      def testValueFormatErrorWith(body: JsValue, expectedPath: String): Unit = testWith(ValueFormatError)(body, expectedPath)

      def testNegativeValueFormatErrorWith(body: JsValue, expectedPath: String): Unit =
        testWith(ValueFormatError.forPathAndRange(expectedPath, min = "-99999999999.99", max = "99999999999.99"))(body, expectedPath)

      "passed a body with an entry containing an invalid amount" when {

        val badValue = JsNumber(42.768)
        List(
          "/income/rentIncome/rentAmount",
          "/income/premiumsOfLeaseGrant",
          "/income/otherPropertyIncome",
          "/income/foreignTaxPaidOrDeducted",
          "/income/specialWithholdingTaxOrUkTaxPaid",
          "/expenses/residentialFinancialCost",
          "/expenses/broughtFwdResidentialFinancialCost"
        ).foreach(path => testValueFormatErrorWith(bodyWith(entry.update(path, badValue)), s"/foreignProperty/0$path"))
        List(
          "/expenses/premisesRunningCosts",
          "/expenses/repairsAndMaintenance",
          "/expenses/financialCosts",
          "/expenses/professionalFees",
          "/expenses/travelCosts",
          "/expenses/costOfServices",
          "/expenses/other"
        ).foreach(path => testNegativeValueFormatErrorWith(bodyWith(entry.update(path, badValue)), s"/foreignProperty/0$path"))

        testNegativeValueFormatErrorWith(
          bodyWith(entryConsolidated.update("/expenses/consolidatedExpenses", badValue)),
          "/foreignProperty/0/expenses/consolidatedExpenses")
      }

      "passed a body with multiple fields containing invalid amounts" in {

        val path0    = "/foreignProperty/0/expenses/premisesRunningCosts"
        val path1    = "/foreignProperty/0/expenses/repairsAndMaintenance"
        val path2    = "/foreignProperty/0/expenses/financialCosts"
        val path3    = "/foreignProperty/0/expenses/travelCosts"
        val badValue = JsNumber(42.768)
        val invalidBody = bodyWith(
          entry
            .update("/expenses/premisesRunningCosts", badValue)
            .update("/expenses/repairsAndMaintenance", badValue)
            .update("/expenses/financialCosts", badValue)
            .update("/expenses/travelCosts", badValue)
        )
        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError
              .forPathAndRange(path0, "-99999999999.99", "99999999999.99")
              .withPaths(
                List(
                  path0,
                  path1,
                  path2,
                  path3
                ))
          ))
      }

      "passed a body with duplicated property IDs" in {
        val invalidBody: JsValue = bodyWith(entry, entry)

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

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

      "passed a body containing a foreignProperty entry with an invalid propertyId format" in {

        val invalidBody: JsValue = bodyWith(entry.update("/propertyId", JsString("invalid-uuid")))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PropertyIdFormatError.withPath("/foreignProperty/0/propertyId")))
      }

      "passed a body containing multiple foreignProperty entries with an invalid propertyId format" in {

        val entryInvalidPropertyId1: JsValue = entryWith("invalid-uuid-1")
        val entryInvalidPropertyId2: JsValue = entryWith("invalid-uuid-2")
        val invalidBody: JsValue             = bodyWith(entryInvalidPropertyId1, entryInvalidPropertyId2)

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            PropertyIdFormatError.withPaths(
              List(
                "/foreignProperty/0/propertyId",
                "/foreignProperty/1/propertyId"
              ))
          ))
      }

      "passed a body containing both foreignProperty expenses" in {

        val invalidBody = bodyWith(entry.update("/expenses/consolidatedExpenses", JsNumber(100.00)))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/foreignProperty/0/expenses")))
      }

      "passed a body containing multiple sub-objects with both expenses" in {

        val entryWithBothExpenses0: JsValue =
          entryWith("5e8b8450-dc1b-4360-8109-7067337b42cb").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val entryWithBothExpenses1: JsValue =
          entryWith("8e8b8450-dc1b-4360-8109-7067337b42cc").update("/expenses/consolidatedExpenses", JsNumber(100.00))
        val invalidBody: JsValue =
          bodyWith(entryWithBothExpenses0, entryWithBothExpenses1).update("/foreignFhlEea/expenses/consolidatedExpenses", JsNumber(100.00))

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
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

      "passed a body with an invalidly formatted toDate and a missing fromDate" in {
        val requestWithInvalidToDateAndMissingFromDate = validBody.update("/toDate", JsString("2024")).removeProperty("/fromDate")

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, requestWithInvalidToDateAndMissingFromDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(ToDateFormatError, RuleMissingSubmissionDatesError))))
      }

      "passed a body with an invalidly formatted fromDate and a missing toDate" in {
        val requestWithInvalidFromDateAndMissingToDate = validBody.update("/fromDate", JsString("2024")).removeProperty("/toDate")

        val result: Either[ErrorWrapper, CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, requestWithInvalidFromDateAndMissingToDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(FromDateFormatError, RuleMissingSubmissionDatesError))))
      }
    }
  }

}
