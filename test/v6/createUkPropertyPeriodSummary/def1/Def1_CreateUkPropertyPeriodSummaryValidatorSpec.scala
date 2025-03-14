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

package v6.createUkPropertyPeriodSummary.def1

import common.models.errors.{RuleBothExpensesSuppliedError, RuleToDateBeforeFromDateError}
import config.MockPropertyBusinessConfig
import play.api.libs.json._
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty._
import v6.createUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty._
import v6.createUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom._
import v6.createUkPropertyPeriodSummary.model.request._

class Def1_CreateUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2023-24"
  private val validBusinessId = "XAIS12345678901"

  private val validBody = Json.parse("""
      |{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
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

  private val validBodyConsolidated = validBody
    .removeProperty("/ukFhlProperty/expenses")
    .removeProperty("/ukNonFhlProperty/expenses")
    .update("/ukFhlProperty/expenses/consolidatedExpenses", JsNumber(988.18))
    .update("/ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(988.18))

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

  private val parsedNino       = Nino(validNino)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedBusinessId = BusinessId(validBusinessId)

  private val parsedFhlIncomeRentARoom   = Def1_Create_UkPropertyIncomeRentARoom(Some(532.12))
  private val parsedFhlIncome            = Def1_Create_UkFhlPropertyIncome(Some(5000.99), Some(3123.21), Some(parsedFhlIncomeRentARoom))
  private val parsedFhlIncomeMinimal     = Def1_Create_UkFhlPropertyIncome(Some(5000.99), None, None)
  private val parsedFhlExpensesRentARoom = Def1_Create_UkPropertyExpensesRentARoom(Some(8842.43))

  //@formatter:off
  private val parsedFhlExpenses = Def1_Create_UkFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), None, Some(974.47),
    Some(parsedFhlExpensesRentARoom)
  )

  private val parsedFhlExpensesConsolidated = Def1_Create_UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(988.18), None,None
  )
  //@formatter:on

  private val parsedUkFhlProperty             = Def1_Create_UkFhlProperty(Some(parsedFhlIncome), Some(parsedFhlExpenses))
  private val parsedUkFhlPropertyConsolidated = parsedUkFhlProperty.copy(expenses = Some(parsedFhlExpensesConsolidated))
  private val parsedUkFhlPropertyMinimal      = parsedUkFhlProperty.copy(income = Some(parsedFhlIncomeMinimal), expenses = None)

  private val parsedNonFhlIncomeRentARoom = Def1_Create_UkPropertyIncomeRentARoom(Some(947.66))

  //@formatter:off
  private val parsedNonFhlIncome = Def1_Create_UkNonFhlPropertyIncome(
    Some(42.12), Some(84.31), Some(9884.93),
    Some(842.99), Some(31.44), Some(parsedNonFhlIncomeRentARoom)
  )

  private val parsedNonFhlIncomeMinimal = Def1_Create_UkNonFhlPropertyIncome(
    None, None, Some(9884.93),
    None, None, None
  )
  //@formatter:on

  private val parsedNonFhlExpensesRentARoom = Def1_Create_UkPropertyExpensesRentARoom(Some(8842.43))

  //@formatter:off
  private val parsedNonFhlExpenses = Def1_Create_UkNonFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), Some(12.34), Some(974.47),
    Some(12.34), Some(parsedNonFhlExpensesRentARoom), None
  )

  private val parsedNonFhlExpensesConsolidated = Def1_Create_UkNonFhlPropertyExpenses(
    None, None, None, None, None, None, None, None, None, None, Some(988.18)
  )
  //@formatter:on

  private val parsedUkNonFhlProperty = Def1_Create_UkNonFhlProperty(
    Some(parsedNonFhlIncome),
    Some(parsedNonFhlExpenses)
  )

  private val parsedUkNonFhlPropertyConsolidated = parsedUkNonFhlProperty.copy(expenses = Some(parsedNonFhlExpensesConsolidated))
  private val parsedUkNonFhlPropertyMinimal      = parsedUkNonFhlProperty.copy(income = Some(parsedNonFhlIncomeMinimal), expenses = None)

  private val parsedBody =
    Def1_CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", Some(parsedUkFhlProperty), Some(parsedUkNonFhlProperty))

  private val parsedBodyConsolidated =
    parsedBody.copy(ukFhlProperty = Some(parsedUkFhlPropertyConsolidated), ukNonFhlProperty = Some(parsedUkNonFhlPropertyConsolidated))

  private val parsedBodyMinimalFhl    = parsedBody.copy(ukFhlProperty = Some(parsedUkFhlPropertyMinimal), ukNonFhlProperty = None)
  private val parsedBodyMinimalNonFhl = parsedBody.copy(ukFhlProperty = None, ukNonFhlProperty = Some(parsedUkNonFhlPropertyMinimal))

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with a consolidated body" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyConsolidated))
      }

      "passed a valid request with a minimal fhl body" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validMinimalFhlBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalFhl))
      }

      "passed a valid request with a minimal non-fhl body" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validMinimalNonFhlBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBodyMinimalNonFhl))
      }

      "passed the minimum supported taxYear" in new SetupConfig {
        val taxYearString = "2022-23"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }

      "passed the maximum supported taxYear" in new SetupConfig {
        val taxYearString = "2023-24"
        validator(validNino, validBusinessId, taxYearString, validBody).validateAndWrapResult() shouldBe
          Right(Def1_CreateUkPropertyPeriodSummaryRequestData(parsedNino, parsedBusinessId, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator("invalid", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted taxYear" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "202324", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))

      }

      "passed an incorrectly formatted businessId" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed a taxYear immediately before the minimum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2021-22", validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear immediately after the maximum supported" in new SetupConfig {
        validator(validNino, validBusinessId, "2025-26", validBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a taxYear spanning an invalid tax year range" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed an invalid business id" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an empty body" in new SetupConfig {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body without ukFhlProperty or ukNonFhlProperty" in new SetupConfig {
        val invalidBody = validBody
          .removeProperty("/ukFhlProperty")
          .removeProperty("ukNonFhlProperty")

        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(path: String, body: JsValue): Unit = {
        s"for $path" in new SetupConfig {
          val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(path)))
        }
      }

      def testRuleIncorrectOrEmptyBodyErrorWith(path: String, body: JsValue): Unit = testWith(RuleIncorrectOrEmptyBodyError)(path, body)

      def testValueFormatErrorWith(path: String, body: JsValue): Unit = testWith(ValueFormatError)(path, body)

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

      "passed a body with an empty object except for an additional (non-schema) property" in new SetupConfig {
        val invalidBody = validBody.replaceWithEmptyObject("/ukFhlProperty").update("/ukFhlProperty/badField", JsNumber(100))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukFhlProperty")))
      }

      "passed a body missing the fromDate" in new SetupConfig {
        val invalidBody = validBody.removeProperty("/fromDate")
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/fromDate")))
      }

      "passed a body missing the toDate" in new SetupConfig {
        val invalidBody = validBody.removeProperty("/toDate")
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/toDate")))
      }

      "passed a body with an invalidly formatted fromDate" in new SetupConfig {
        val invalidBody = validBody.update("/fromDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with an invalidly formatted toDate" in new SetupConfig {
        val invalidBody = validBody.update("/toDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a fromDate that precedes the minimum" in new SetupConfig {
        val invalidBody = validBody.update("/fromDate", JsString("1569-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a toDate that proceeds the minimum" in new SetupConfig {
        val invalidBody = validBody.update("/toDate", JsString("3490-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate that precedes the fromDate" in new SetupConfig {
        val invalidBody = validBody.update("/fromDate", JsString("2090-10-01"))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body with a (non-consolidated expenses) field containing an invalid value" when {
        List(
          "/ukFhlProperty/income/periodAmount",
          "/ukFhlProperty/income/taxDeducted",
          "/ukFhlProperty/income/rentARoom/rentsReceived",
          "/ukFhlProperty/expenses/premisesRunningCosts",
          "/ukFhlProperty/expenses/repairsAndMaintenance",
          "/ukFhlProperty/expenses/financialCosts",
          "/ukFhlProperty/expenses/professionalFees",
          "/ukFhlProperty/expenses/costOfServices",
          "/ukFhlProperty/expenses/other",
          "/ukFhlProperty/expenses/travelCosts",
          "/ukFhlProperty/expenses/rentARoom/amountClaimed",
          "/ukNonFhlProperty/income/premiumsOfLeaseGrant",
          "/ukNonFhlProperty/income/reversePremiums",
          "/ukNonFhlProperty/income/periodAmount",
          "/ukNonFhlProperty/income/taxDeducted",
          "/ukNonFhlProperty/income/otherIncome",
          "/ukNonFhlProperty/income/rentARoom/rentsReceived",
          "/ukNonFhlProperty/expenses/premisesRunningCosts",
          "/ukNonFhlProperty/expenses/repairsAndMaintenance",
          "/ukNonFhlProperty/expenses/financialCosts",
          "/ukNonFhlProperty/expenses/professionalFees",
          "/ukNonFhlProperty/expenses/costOfServices",
          "/ukNonFhlProperty/expenses/other",
          "/ukNonFhlProperty/expenses/residentialFinancialCost",
          "/ukNonFhlProperty/expenses/travelCosts",
          "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward",
          "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"
        ).foreach(path => testValueFormatErrorWith(path, validBody.update(path, JsNumber(123.456))))
      }

      "passed a body with a (consolidated expenses) field containing an invalid value" when {
        List(
          "/ukFhlProperty/expenses/consolidatedExpenses",
          "/ukNonFhlProperty/expenses/consolidatedExpenses"
        ).foreach(path => testValueFormatErrorWith(path, validBodyConsolidated.update(path, JsNumber(123.456))))
      }

      "passed a body with multiple invalid fields" in new SetupConfig {
        val path0 = "/ukFhlProperty/expenses/travelCosts"
        val path1 = "/ukNonFhlProperty/expenses/travelCosts"
        val path2 = "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"

        val invalidBody = validBody
          .update(path0, JsNumber(123.456))
          .update(path1, JsNumber(123.456))
          .update(path2, JsNumber(123.456))

        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path0, path1, path2))))
      }

      "passed a body with both consolidated and separate expenses provided for fhl" in new SetupConfig {
        val invalidBody = validBody.update("ukFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses")))
      }

      "passed a body with both consolidated and separate expenses provided for non-fhl" in new SetupConfig {
        val invalidBody = validBody.update("ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))
        val result: Either[ErrorWrapper, CreateUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {
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
