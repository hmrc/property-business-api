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

package v5.createAmendUkPropertyCumulativeSummary.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json}
import support.UnitSpec
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request._
import v5.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData

class Def1_CreateAmendUkPropertyCumulativeSummaryValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2025-26"
  private val validBusinessId = "XAIS12345678901"

  private val fullRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "rentARoom": {
      |        "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 1500.50,
      |      "repairsAndMaintenance": 1200.75,
      |      "financialCosts": 2000.00,
      |      "professionalFees": 500.00,
      |      "costOfServices": 300.25,
      |      "other": 100.50,
      |      "residentialFinancialCost": 9000.10,
      |      "travelCosts": 400.00,
      |      "residentialFinancialCostsCarriedForward": 300.13,
      |      "rentARoom": {
      |        "amountClaimed": 860.88
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val fullRequestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
    Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
      fromDate = Some("2023-04-01"),
      toDate = Some("2024-04-01"),
      ukProperty = UkProperty(
        income = Some(
          Income(
            premiumsOfLeaseGrant = Some(42.12),
            reversePremiums = Some(84.31),
            periodAmount = Some(9884.93),
            taxDeducted = Some(842.99),
            otherIncome = Some(31.44),
            rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
          )
        ),
        expenses = Some(
          Expenses(
            premisesRunningCosts = Some(1500.50),
            repairsAndMaintenance = Some(1200.75),
            financialCosts = Some(2000.00),
            professionalFees = Some(500.00),
            costOfServices = Some(300.25),
            other = Some(100.50),
            residentialFinancialCost = Some(9000.10),
            travelCosts = Some(400.00),
            residentialFinancialCostsCarriedForward = Some(300.13),
            rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
            consolidatedExpenses = None
          )
        )
      )
    )

  private val validConsolidatedBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "rentARoom": {
      |        "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "residentialFinancialCost": 9000.10,
      |      "residentialFinancialCostsCarriedForward": 300.13,
      |      "rentARoom": {
      |        "amountClaimed": 860.88
      |      },
      |      "consolidatedExpenses": -988.18
      |    }
      |  }
      |}
      """.stripMargin
  )

  private val consolidatedRequestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
    Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
      fromDate = Some("2023-04-01"),
      toDate = Some("2024-04-01"),
      ukProperty = UkProperty(
        income = Some(
          Income(
            premiumsOfLeaseGrant = Some(42.12),
            reversePremiums = Some(84.31),
            periodAmount = Some(9884.93),
            taxDeducted = Some(842.99),
            otherIncome = Some(31.44),
            rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
          )
        ),
        expenses = Some(
          Expenses(
            premisesRunningCosts = None,
            repairsAndMaintenance = None,
            financialCosts = None,
            professionalFees = None,
            costOfServices = None,
            other = None,
            residentialFinancialCost = Some(9000.10),
            travelCosts = None,
            residentialFinancialCostsCarriedForward = Some(300.13),
            rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
            consolidatedExpenses = Some(-988.18)
          )
        )
      )
    )

  private val parsedNino       = Nino(validNino)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedBusinessId = BusinessId(validBusinessId)

  private def validator(nino: String, taxYear: String, businessId: String, body: JsValue) =
    new Def1_CreateAmendUkPropertyCumulativeSummaryValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, fullRequestJson).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, fullRequestBody))
      }

      "passed a valid request with a consolidated" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validConsolidatedBodyJson).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, consolidatedRequestBody))
      }

      "passed a request with no 'from' and 'to' dates" in {
        val requestWithoutDates = fullRequestJson.as[JsObject] - "fromDate" - "toDate"

        validator(validNino, validTaxYear, validBusinessId, requestWithoutDates).validateAndWrapResult() shouldBe
          Right(
            Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(
              parsedNino,
              parsedTaxYear,
              parsedBusinessId,
              fullRequestBody.copy(fromDate = None, toDate = None))
          )
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator("invalid", validTaxYear, validBusinessId, fullRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, "202324", validBusinessId, fullRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, "invalid", fullRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, "2020-22", validBusinessId, fullRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(path: String, body: JsValue): Unit = {
        s"for $path" in {
          val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
            validator(validNino, validTaxYear, validBusinessId, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(path)))
        }
      }

      def testRuleIncorrectOrEmptyBodyErrorWith(path: String, body: JsValue): Unit = testWith(RuleIncorrectOrEmptyBodyError)(path, body)

      def testValueFormatErrorWith(path: String, body: JsValue): Unit = testWith(ValueFormatError)(path, body)

      def testNegativeValueFormatErrorWith(path: String, body: JsValue): Unit =
        testWith(ValueFormatError.forPathAndRange(path, min = "-99999999999.99", max = "99999999999.99"))(path, body)

      "passed a body with an empty object" when {
        List(
          "/ukProperty",
          "/ukProperty/income",
          "/ukProperty/expenses"
        ).foreach(path => testRuleIncorrectOrEmptyBodyErrorWith(path, fullRequestJson.replaceWithEmptyObject(path)))
      }

      "passed a body with an empty object except for an additional (non-schema) property" in {
        val invalidBody = fullRequestJson.replaceWithEmptyObject("/ukProperty").update("/ukProperty/badField", JsNumber(100))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukProperty")))
      }

      "passed a body with an invalidly formatted fromDate" in {
        val invalidBody = fullRequestJson.update("/fromDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in {
        val invalidBody = fullRequestJson.update("/toDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a fromDate that precedes the minimum" in {
        val invalidBody = fullRequestJson.update("/fromDate", JsString("1569-10-01"))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a toDate after maximum" in {
        val invalidBody = fullRequestJson.update("/toDate", JsString("3490-10-01"))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate that precedes the fromDate" in {
        val invalidBody = fullRequestJson.update("/fromDate", JsString("2090-10-01"))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a missing fromDate" in {
        val requestWithoutFromDate = fullRequestJson.removeProperty("/fromDate")

        validator(validNino, validTaxYear, validBusinessId, requestWithoutFromDate).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleMissingSubmissionDatesError))
      }

      "passed a body with a missing toDate" in {
        val requestWithoutToDate = fullRequestJson.removeProperty("/toDate")

        validator(validNino, validTaxYear, validBusinessId, requestWithoutToDate).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleMissingSubmissionDatesError))
      }

      "passed a body with a (non-consolidated expenses) field containing an invalid value" when {

        List(
          "/ukProperty/income/premiumsOfLeaseGrant",
          "/ukProperty/income/reversePremiums",
          "/ukProperty/income/periodAmount",
          "/ukProperty/income/taxDeducted",
          "/ukProperty/income/otherIncome",
          "/ukProperty/income/rentARoom/rentsReceived",
          "/ukProperty/expenses/rentARoom/amountClaimed",
          "/ukProperty/expenses/residentialFinancialCost",
          "/ukProperty/expenses/residentialFinancialCostsCarriedForward"
        ).foreach(path => testValueFormatErrorWith(path, fullRequestJson.update(path, JsNumber(123.456))))

        List(
          "/ukProperty/expenses/premisesRunningCosts",
          "/ukProperty/expenses/repairsAndMaintenance",
          "/ukProperty/expenses/financialCosts",
          "/ukProperty/expenses/professionalFees",
          "/ukProperty/expenses/costOfServices",
          "/ukProperty/expenses/other",
          "/ukProperty/expenses/travelCosts"
        ).foreach(path => testNegativeValueFormatErrorWith(path, fullRequestJson.update(path, JsNumber(123.456))))
      }

      "passed a body with multiple invalid fields" in {
        val path0 = "/ukProperty/expenses/travelCosts"
        val path1 = "/ukProperty/expenses/other"
        val path2 = "/ukProperty/expenses/costOfServices"

        val invalidBody = fullRequestJson
          .update(path0, JsNumber(123.456))
          .update(path1, JsNumber(123.456))
          .update(path2, JsNumber(123.456))

        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange(path0, min = "-99999999999.99", max = "99999999999.99").withPaths(List(path2, path1, path0))))
      }

      "passed a body with both consolidated and separate expenses provided" in {
        val invalidBody = fullRequestJson.update("ukProperty/expenses/consolidatedExpenses", JsNumber(123.45))
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukProperty/expenses")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateAmendUkPropertyCumulativeSummaryRequestData] =
          validator("invalid", "invalid", "invalid", fullRequestJson).validateAndWrapResult()

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
