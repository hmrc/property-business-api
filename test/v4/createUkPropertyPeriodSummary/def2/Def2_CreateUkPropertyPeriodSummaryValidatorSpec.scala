/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.createUkPropertyPeriodSummary.def2

import common.models.errors.{RuleBothExpensesSuppliedError, RuleToDateBeforeFromDateError}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import shared.utils.UnitSpec
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty._
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty._
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom._
import v4.createUkPropertyPeriodSummary.model.request._

class Def2_CreateUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2024-25"
  private val validBusinessId = "XAIS12345678901"

  private val validBody = Json.parse("""
     |{
     |    "fromDate": "2024-04-06",
     |    "toDate": "2024-07-05",
     |    "ukFhlProperty":{
     |        "income": {
     |            "periodAmount": 5000.99,
     |            "taxDeducted": 3123.21,
     |            "rentARoom": {
     |                "rentsReceived": 532.12
     |            }
     |        },
     |        "expenses": {
     |            "premisesRunningCosts": 3123.21,
     |            "repairsAndMaintenance": 928.42,
     |            "financialCosts": 842.99,
     |            "professionalFees": 8831.12,
     |            "costOfServices": 484.12,
     |            "other": 99282,
     |            "travelCosts": 974.47,
     |            "rentARoom": {
     |                "amountClaimed": 8842.43
     |            }
     |        }
     |    },
     |    "ukNonFhlProperty": {
     |        "income": {
     |            "premiumsOfLeaseGrant": 42.12,
     |            "reversePremiums": 84.31,
     |            "periodAmount": 9884.93,
     |            "taxDeducted": 842.99,
     |            "otherIncome": 31.44,
     |            "rentARoom": {
     |                "rentsReceived": 947.66
     |            }
     |        },
     |        "expenses": {
     |            "premisesRunningCosts": 3123.21,
     |            "repairsAndMaintenance": 928.42,
     |            "financialCosts": 842.99,
     |            "professionalFees": 8831.12,
     |            "costOfServices": 484.12,
     |            "other": 99282,
     |            "residentialFinancialCost": 12.34,
     |            "travelCosts": 974.47,
     |            "residentialFinancialCostsCarriedForward": 12.34,
     |            "rentARoom": {
     |                "amountClaimed": 8842.43
     |            }
     |        }
     |    }
     |}
     |""".stripMargin)

  private val validMinimalFhlBody = validBody
    .removeProperty("/ukNonFhlProperty")
    .replaceWithEmptyObject("/ukFhlProperty")
    .update(
      "/ukFhlProperty",
      Json.parse("""
               | {
               |   "income": {
               |     "periodAmount": 5000.99
               |   }
               | }
               |""".stripMargin)
    )

  private val validMinimalNonFhlBody = validBody
    .removeProperty("/ukFhlProperty")
    .replaceWithEmptyObject("/ukNonFhlProperty")
    .update(
      "/ukNonFhlProperty",
      Json.parse("""
               | {
               |   "income": {
               |     "periodAmount": 9884.93
               |   }
               | }
               |""".stripMargin)
    )

  private val validBodyConsolidatedWithExtraFields = validBody
    .removeProperty("/ukFhlProperty/expenses")
    .removeProperty("/ukNonFhlProperty/expenses")
    .update("/ukFhlProperty/expenses/consolidatedExpenses", JsNumber(988.18))
    .update("/ukFhlProperty/expenses/rentARoom/amountClaimed", JsNumber(900.01))
    .update("/ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(988.18))
    .update("/ukNonFhlProperty/expenses/residentialFinancialCost", JsNumber(3000.01))
    .update("/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward", JsNumber(200.37))
    .update("/ukNonFhlProperty/expenses/rentARoom/amountClaimed", JsNumber(935.01))

  private val parsedNino       = Nino(validNino)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedBusinessId = BusinessId(validBusinessId)

  private val parsedFhlIncomeRentARoom   = Def2_Create_UkPropertyIncomeRentARoom(Some(532.12))
  private val parsedFhlIncome            = Def2_Create_UkFhlPropertyIncome(Some(5000.99), Some(3123.21), Some(parsedFhlIncomeRentARoom))
  private val parsedFhlIncomeMinimal     = Def2_Create_UkFhlPropertyIncome(Some(5000.99), None, None)
  private val parsedFhlExpensesRentARoom = Def2_Create_UkPropertyExpensesRentARoom(Some(8842.43))

  //@formatter:off
  private val parsedFhlExpenses = Def2_Create_UkFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), None, Some(974.47),
    Some(parsedFhlExpensesRentARoom)
  )

  private val parsedFhlExpensesConsolidated = Def2_Create_UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(988.18), None, Some(Def2_Create_UkPropertyExpensesRentARoom(Some(900.01)))
  )
  //@formatter:on

  private val parsedUkFhlProperty             = Def2_Create_UkFhlProperty(Some(parsedFhlIncome), Some(parsedFhlExpenses))
  private val parsedUkFhlPropertyConsolidated = parsedUkFhlProperty.copy(expenses = Some(parsedFhlExpensesConsolidated))
  private val parsedUkFhlPropertyMinimal      = parsedUkFhlProperty.copy(income = Some(parsedFhlIncomeMinimal), expenses = None)

  private val parsedNonFhlIncomeRentARoom = Def2_Create_UkPropertyIncomeRentARoom(Some(947.66))

  //@formatter:off
  private val parsedNonFhlIncome = Def2_Create_UkNonFhlPropertyIncome(
    Some(42.12), Some(84.31), Some(9884.93),
    Some(842.99), Some(31.44), Some(parsedNonFhlIncomeRentARoom)
  )

  private val parsedNonFhlIncomeMinimal = Def2_Create_UkNonFhlPropertyIncome(
    None, None, Some(9884.93),
    None, None, None
  )
  //@formatter:on

  private val parsedNonFhlExpensesRentARoom = Def2_Create_UkPropertyExpensesRentARoom(Some(8842.43))

  //@formatter:off
  private val parsedNonFhlExpenses = Def2_Create_UkNonFhlPropertyExpensesSubmission(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), Some(12.34), None, Some(974.47),
    Some(12.34), None, Some(parsedNonFhlExpensesRentARoom), None
  )

  private val parsedNonFhlExpensesConsolidated = Def2_Create_UkNonFhlPropertyExpensesSubmission(
    None, None, None, None, None, None, None, Some(3000.01), None, None, Some(200.37), Some(Def2_Create_UkPropertyExpensesRentARoom(Some(935.01))), Some(988.18)
  )

  //@formatter:on

  private val parsedUkNonFhlProperty = Def2_Create_UkNonFhlPropertySubmission(
    Some(parsedNonFhlIncome),
    Some(parsedNonFhlExpenses)
  )

  private val parsedUkNonFhlPropertyConsolidated = parsedUkNonFhlProperty.copy(expenses = Some(parsedNonFhlExpensesConsolidated))
  private val parsedUkNonFhlPropertyMinimal      = parsedUkNonFhlProperty.copy(income = Some(parsedNonFhlIncomeMinimal), expenses = None)

  private val parsedBody =
    Def2_CreateUkPropertyPeriodSummarySubmissionRequestBody("2024-04-06", "2024-07-05", Some(parsedUkFhlProperty), Some(parsedUkNonFhlProperty))

  private val parsedBodyConsolidated =
    parsedBody.copy(ukFhlProperty = Some(parsedUkFhlPropertyConsolidated), ukNonFhlProperty = Some(parsedUkNonFhlPropertyConsolidated))

  private val parsedBodyMinimalFhl    = parsedBody.copy(ukFhlProperty = Some(parsedUkFhlPropertyMinimal), ukNonFhlProperty = None)
  private val parsedBodyMinimalNonFhl = parsedBody.copy(ukFhlProperty = None, ukNonFhlProperty = Some(parsedUkNonFhlPropertyMinimal))

  private def validator(nino: String, taxYear: String, businessId: String, body: JsValue) =
    new Def2_CreateUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Right(Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with a consolidated body for 2024-25" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validBodyConsolidatedWithExtraFields).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyConsolidated))
      }

      "passed a valid request with a minimal fhl body" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validMinimalFhlBody).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalFhl))
      }

      "passed a valid request with a minimal non-fhl body" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, validMinimalNonFhlBody).validateAndWrapResult()

        result shouldBe Right(
          Def2_CreateUkPropertyPeriodSummarySubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalNonFhl))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator("invalid", validTaxYear, validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "202324", validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear spanning an invalid tax year range" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2020-22", validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a taxYear immediately after the maximum tax year" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2025-26", validBusinessId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an invalid business id" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body without ukFhlProperty or ukNonFhlProperty" in {
        val invalidBody = validBody
          .removeProperty("/ukFhlProperty")
          .removeProperty("ukNonFhlProperty")

        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(path: String, body: JsValue): Unit = {
        s"for $path" in {
          val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
            validator(validNino, validTaxYear, validBusinessId, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(path)))
        }
      }

      def testRuleIncorrectOrEmptyBodyErrorWith(path: String, body: JsValue): Unit = testWith(RuleIncorrectOrEmptyBodyError)(path, body)
      def testValueFormatErrorWith(path: String, body: JsValue): Unit              = testWith(ValueFormatError)(path, body)
      def testNegativeValueFormatErrorWith(path: String, body: JsValue): Unit =
        testWith(ValueFormatError.forPathAndRange(path, min = "-99999999999.99", max = "99999999999.99"))(path, body)

      "passed a body with an empty object" when {
        List(
          "/ukFhlProperty",
          "/ukFhlProperty/income",
          "/ukFhlProperty/income/rentARoom",
          "/ukFhlProperty/expenses",
          "/ukFhlProperty/expenses/rentARoom",
          "/ukNonFhlProperty",
          "/ukNonFhlProperty/income",
          "/ukNonFhlProperty/income/rentARoom",
          "/ukNonFhlProperty/expenses",
          "/ukNonFhlProperty/expenses/rentARoom"
        ).foreach(path => testRuleIncorrectOrEmptyBodyErrorWith(path, validBody.replaceWithEmptyObject(path)))
      }

      "passed a body with an empty object except for an additional (non-schema) property" in {
        val invalidBody = validBody.replaceWithEmptyObject("/ukFhlProperty").update("/ukFhlProperty/badField", JsNumber(100))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukFhlProperty")))
      }

      "passed a body missing the fromDate" in {
        val invalidBody = validBody.removeProperty("/fromDate")
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/fromDate")))
      }

      "passed a body missing the toDate" in {
        val invalidBody = validBody.removeProperty("/toDate")
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/toDate")))
      }

      "passed a body with an invalidly formatted fromDate" in {
        val invalidBody = validBody.update("/fromDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in {
        val invalidBody = validBody.update("/toDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a fromDate that precedes the minimum" in {
        val invalidBody = validBody.update("/fromDate", JsString("1569-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a toDate that proceeds the minimum" in {
        val invalidBody = validBody.update("/toDate", JsString("3490-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate that precedes the fromDate" in {
        val invalidBody = validBody.update("/fromDate", JsString("2090-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a (non-consolidated expenses) field containing an invalid value" when {

        List(
          "/ukFhlProperty/income/periodAmount",
          "/ukFhlProperty/income/taxDeducted",
          "/ukFhlProperty/income/rentARoom/rentsReceived"
        ).foreach(path => testValueFormatErrorWith(path, validBody.update(path, JsNumber(123.456))))

        List(
          "/ukFhlProperty/expenses/premisesRunningCosts",
          "/ukFhlProperty/expenses/repairsAndMaintenance",
          "/ukFhlProperty/expenses/financialCosts",
          "/ukFhlProperty/expenses/professionalFees",
          "/ukFhlProperty/expenses/costOfServices",
          "/ukFhlProperty/expenses/other",
          "/ukFhlProperty/expenses/travelCosts",
          "/ukNonFhlProperty/expenses/premisesRunningCosts",
          "/ukNonFhlProperty/expenses/repairsAndMaintenance",
          "/ukNonFhlProperty/expenses/financialCosts",
          "/ukNonFhlProperty/expenses/professionalFees",
          "/ukNonFhlProperty/expenses/costOfServices",
          "/ukNonFhlProperty/expenses/other",
          "/ukNonFhlProperty/expenses/travelCosts"
        ).foreach(path => testNegativeValueFormatErrorWith(path, validBody.update(path, JsNumber(123.456))))
      }

      "passed a body with a (consolidated expenses) field containing an invalid value" when {
        List(
          "/ukFhlProperty/expenses/consolidatedExpenses",
          "/ukNonFhlProperty/expenses/consolidatedExpenses"
        ).foreach(path => testNegativeValueFormatErrorWith(path, validBodyConsolidatedWithExtraFields.update(path, JsNumber(123.456))))
      }

      "passed a body with multiple invalid fields" in {
        val path0 = "/ukFhlProperty/expenses/travelCosts"
        val path1 = "/ukNonFhlProperty/expenses/other"
        val path2 = "/ukNonFhlProperty/expenses/travelCosts"

        val invalidBody = validBody
          .update(path0, JsNumber(123.456))
          .update(path1, JsNumber(123.456))
          .update(path2, JsNumber(123.456))

        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange(path0, min = "-99999999999.99", max = "99999999999.99").withPaths(List(path0, path1, path2))))
      }

      "passed a body with both consolidated and separate expenses provided for fhl" in {
        val invalidBody = validBody.update("ukFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses")))
      }

      "passed a body with both consolidated and separate expenses provided for non-fhl" in {
        val invalidBody = validBody.update("ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validTaxYear, validBusinessId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
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
