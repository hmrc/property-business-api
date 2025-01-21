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

package v5.amendUkPropertyPeriodSummary.def2

import common.models.domain.SubmissionId
import common.models.errors.{RuleBothExpensesSuppliedError, SubmissionIdFormatError}
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v5.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty._
import v5.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty._
import v5.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom._
import v5.amendUkPropertyPeriodSummary.model.request._

class Def2_AmendUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2024-25"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val validBody = Json.parse(
    """{
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
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    }
      |}
      |""".stripMargin
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

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val parsedIncomeRentARoom   = Def2_Amend_UkPropertyIncomeRentARoom(Some(532.12))
  private val parsedExpensesRentARoom = Def2_Amend_UkPropertyExpensesRentARoom(Some(8842.43))

  private val parsedFhlIncome        = Def2_Amend_UkFhlPropertyIncome(Some(5000.99), Some(3123.21), Some(parsedIncomeRentARoom))
  private val parsedFhlIncomeMinimal = Def2_Amend_UkFhlPropertyIncome(Some(567.83), None, None)



  // @formatter:off
  private val parsedFhlExpenses = Def2_Amend_UkFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), None, Some(974.47), Some(parsedExpensesRentARoom)
  )

  private val parsedNonFhlIncome = Def2_Amend_UkNonFhlPropertyIncome(
    Some(42.12), Some(84.31), Some(9884.93),
    Some(842.99), Some(31.44), Some(parsedIncomeRentARoom)
  )


  private val parsedNonFhlIncomeMinimal = Def2_Amend_UkNonFhlPropertyIncome(
    None, None, Some(567.83), None, None, None
  )

  private val parsedNonFhlExpenses = Def2_Amend_UkNonFhlPropertyExpensesSubmission(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), Some(12.34), None, Some(974.47),
    Some(12.34), None, Some(parsedExpensesRentARoom), None
  )

  private val parsedFhlExpensesConsolidated = Def2_Amend_UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(988.18), None, Some(Def2_Amend_UkPropertyExpensesRentARoom(Some(900.01))))

  private val parsedNonFhlExpensesConsolidated = Def2_Amend_UkNonFhlPropertyExpensesSubmission(
    None, None, None, None, None,None,None,Some(3000.01),None,None, Some(200.37), Some(Def2_Amend_UkPropertyExpensesRentARoom(Some(935.01))),Some(988.18)
  )
  // @formatter:on

  private val parsedUkFhlProperty = Def2_Amend_UkFhlProperty(
    Some(parsedFhlIncome),
    Some(parsedFhlExpenses)
  )

  private val parsedUkNonFhlProperty = Def2_Amend_UkNonFhlPropertySubmission(
    Some(parsedNonFhlIncome),
    Some(parsedNonFhlExpenses)
  )

  private val parsedBody = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(
    Some(parsedUkFhlProperty),
    Some(parsedUkNonFhlProperty)
  )

  private val parsedBodyConsolidated = parsedBody.copy(
    ukFhlProperty = Some(parsedUkFhlProperty.copy(expenses = Some(parsedFhlExpensesConsolidated))),
    Some(parsedUkNonFhlProperty.copy(expenses = Some(parsedNonFhlExpensesConsolidated)))
  )

  private val parsedBodyMinimalFhl = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(
    Some(Def2_Amend_UkFhlProperty(Some(parsedFhlIncomeMinimal), None)),
    None
  )

  private val parsedBodyMinimalNonFhl = Def2_AmendUkPropertyPeriodSummarySubmissionRequestBody(
    None,
    Some(Def2_Amend_UkNonFhlPropertySubmission(Some(parsedNonFhlIncomeMinimal), None))
  )

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue) = {
    new Def2_AmendUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId, body)
  }

  "validator" should {
    "return the parsed domain object in its submission model" when {
      "passed a valid request" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(parsedNino, parsedTaxYear, parsedBusinessId, parsedSubmissionId, parsedBody))
      }

      "passed a valid consolidated request body for 2024-25 in its submission model" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyConsolidatedWithExtraFields).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(
            parsedNino,
            parsedTaxYear,
            parsedBusinessId,
            parsedSubmissionId,
            parsedBodyConsolidated))
      }

      "passed a valid request with minimal fhl in its submission model" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            validSubmissionId,
            Json.parse("""
                |{
                |  "ukFhlProperty": {
                |    "income": {
                |       "periodAmount": 567.83
                |    }
                |  }
                |}
                |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(
            parsedNino,
            parsedTaxYear,
            parsedBusinessId,
            parsedSubmissionId,
            parsedBodyMinimalFhl))
      }

      "passed a valid request with minimal non-fhl" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(
            validNino,
            validBusinessId,
            validTaxYear,
            validSubmissionId,
            Json.parse("""
                |{
                |  "ukNonFhlProperty": {
                |    "income": {
                |      "periodAmount": 567.83
                |    }
                |  }
                |}
                |""".stripMargin)
          ).validateAndWrapResult()

        result shouldBe Right(
          Def2_AmendUkPropertyPeriodSummarySubmissionRequestData(
            parsedNino,
            parsedTaxYear,
            parsedBusinessId,
            parsedSubmissionId,
            parsedBodyMinimalNonFhl))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator("invalid", validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted taxYear" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "202324", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an invalid tax year range" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2020-22", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a tax year immediately after the maximum supported" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2025-26", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an invalid business id" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "invalid", validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "passed an invalid submission id" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }

      "passed an empty body" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      def testWith(error: MtdError)(body: JsValue, path: String): Unit =
        s"for $path" in {

          val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
            validator(validNino, validBusinessId, validTaxYear, validSubmissionId, body)
              .validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error.withPath(path)))
        }

      def testRuleIncorrectOrEmptyBodyWith(path: String): Unit =
        testWith(RuleIncorrectOrEmptyBodyError)(validBody.removeProperty(path).update(path, JsObject.empty), path)

      def testValueFormatErrorWith(body: JsValue, path: String): Unit = testWith(ValueFormatError)(body, path)

      def testValueFormatErrorWithNegativeValue(body: JsValue, path: String): Unit =
        testWith(ValueFormatError.forPathAndRange(path, "-99999999999.99", "99999999999.99"))(body, path)

      "passed a body with an empty sub-object" when {

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
        ).foreach(testRuleIncorrectOrEmptyBodyWith)
      }

      "passed a body with an empty sub-object except for an additional (non-schema) property" in {

        val invalidBody = Json.parse("""
            |{
            |    "ukFhlProperty":{
            |       "unknownField": 999.99
            |    }
            |}""".stripMargin)

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/ukFhlProperty")))
      }

      "passed a body with invalid income or (non-consolidated) expenses" when {

        List(
          "/ukFhlProperty/income/periodAmount",
          "/ukFhlProperty/income/taxDeducted",
          "/ukFhlProperty/income/rentARoom/rentsReceived",
          "/ukFhlProperty/expenses/rentARoom/amountClaimed",
          "/ukNonFhlProperty/income/rentARoom/rentsReceived",
          "/ukNonFhlProperty/expenses/residentialFinancialCost",
          "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward",
          "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"
        ).foreach(path => testValueFormatErrorWith(validBody.update(path, JsNumber(123.456)), path))

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
        ).foreach(path => testValueFormatErrorWithNegativeValue(validBody.update(path, JsNumber(123.456)), path))
      }

      "passed a body with invalid consolidated expenses" when {

        List(
          "/ukFhlProperty/expenses/consolidatedExpenses",
          "/ukNonFhlProperty/expenses/consolidatedExpenses"
        ).foreach(path => testValueFormatErrorWithNegativeValue(validBodyConsolidatedWithExtraFields.update(path, JsNumber(123.456)), path))
      }

      "passed a body with multiple invalid fields" in {

        val path0 = "/ukFhlProperty/expenses/travelCosts"
        val path1 = "/ukNonFhlProperty/expenses/premisesRunningCosts"
        val path2 = "/ukNonFhlProperty/expenses/travelCosts"

        val invalidBody = validBody
          .update(path0, JsNumber(123.456))
          .update(path1, JsNumber(123.456))
          .update(path2, JsNumber(123.456))

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange(path0, "-99999999999.99", "99999999999.99").withPaths(List(path0, path1, path2))))
      }

      "passed a body with both consolidated and separate expenses for fhl" in {

        val invalidBody = validBody.update("ukFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses")))
      }

      "passed a body with both consolidated and separate expenses for non-fhl" in {

        val invalidBody = validBody.update("ukNonFhlProperty/expenses/consolidatedExpenses", JsNumber(123.45))

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
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
