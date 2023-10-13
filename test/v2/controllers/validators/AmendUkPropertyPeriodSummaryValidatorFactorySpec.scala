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

import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import api.support.UnitSpec
import v2.models.request.amendUkPropertyPeriodSummary.{AmendUkPropertyPeriodSummaryRequestBody, AmendUkPropertyPeriodSummaryRequestData}
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

class AmendUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validTaxYear      = "2022-23"
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

  private val validBodyConsolidated = Json.parse(
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
      |            "consolidatedExpenses": 988.18
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
      |            "consolidatedExpenses": 988.18
      |        }
      |    }
      |}
      |""".stripMargin
  )

  private val parsedNino         = Nino(validNino)
  private val parsedBusinessId   = BusinessId(validBusinessId)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSubmissionId = SubmissionId(validSubmissionId)

  private val parsedIncomeRentARoom   = UkPropertyIncomeRentARoom(Some(532.12))
  private val parsedExpensesRentARoom = UkPropertyExpensesRentARoom(Some(8842.43))

  private val parsedFhlIncome        = UkFhlPropertyIncome(Some(5000.99), Some(3123.21), Some(parsedIncomeRentARoom))
  private val parsedFhlIncomeMinimal = UkFhlPropertyIncome(Some(567.83), None, None)

  // @formatter:off
  private val parsedFhlExpenses = UkFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), None, Some(974.47), Some(parsedExpensesRentARoom)
  )

  private val parsedNonFhlIncome = UkNonFhlPropertyIncome(
    Some(42.12), Some(84.31), Some(9884.93),
    Some(842.99), Some(31.44), Some(parsedIncomeRentARoom)
  )

  private val parsedNonFhlIncomeMinimal = UkNonFhlPropertyIncome(
    None, None, Some(567.83), None, None, None
  )

  private val parsedNonFhlExpenses = UkNonFhlPropertyExpenses(
    Some(3123.21), Some(928.42), Some(842.99), Some(8831.12),
    Some(484.12), Some(99282), Some(12.34), Some(974.47),
    Some(12.34), Some(parsedExpensesRentARoom), None
  )

  private val parsedFhlExpensesConsolidated = UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(988.18), None, None
  )

  private val parsedNonFhlExpensesConsolidated = UkNonFhlPropertyExpenses(
    None, None, None, None, None,None,None,None, None, None,Some(988.18) 
  )
  // @formatter:on

  private val parsedUkFhlProperty = UkFhlProperty(
    Some(parsedFhlIncome),
    Some(parsedFhlExpenses)
  )

  private val parsedUkNonFhlProperty = UkNonFhlProperty(
    Some(parsedNonFhlIncome),
    Some(parsedNonFhlExpenses)
  )

  private val parsedBody = AmendUkPropertyPeriodSummaryRequestBody(
    Some(parsedUkFhlProperty),
    Some(parsedUkNonFhlProperty)
  )

  private val parsedBodyConsolidated = parsedBody.copy(
    ukFhlProperty = Some(parsedUkFhlProperty.copy(expenses = Some(parsedFhlExpensesConsolidated))),
    Some(parsedUkNonFhlProperty.copy(expenses = Some(parsedNonFhlExpensesConsolidated)))
  )

  private val parsedBodyMinimalFhl = AmendUkPropertyPeriodSummaryRequestBody(
    Some(UkFhlProperty(Some(parsedFhlIncomeMinimal), None)),
    None
  )

  private val parsedBodyMinimalNonFhl = AmendUkPropertyPeriodSummaryRequestBody(
    None,
    Some(UkNonFhlProperty(Some(parsedNonFhlIncomeMinimal), None))
  )

  private val validatorFactory = new AmendUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue) =
    validatorFactory.validator(nino, businessId, taxYear, submissionId, body)

  MockAppConfig.minimumTaxV2Uk.returns(2021)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Right(AmendUkPropertyPeriodSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, parsedSubmissionId, parsedBody))
      }

      "passed a valid consolidated request" in {
        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(
          AmendUkPropertyPeriodSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, parsedSubmissionId, parsedBodyConsolidated))
      }

      "passed a valid request with minimal fhl" in {
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
          AmendUkPropertyPeriodSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, parsedSubmissionId, parsedBodyMinimalFhl))
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
          AmendUkPropertyPeriodSummaryRequestData(parsedNino, parsedTaxYear, parsedBusinessId, parsedSubmissionId, parsedBodyMinimalNonFhl))
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
          validator(validNino, validBusinessId, "2020", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an unsupported tax year" in {
        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2019-20", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an invalid tax year range" in {
        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, "2019-21", validSubmissionId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
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
        ).foreach(path => testValueFormatErrorWith(validBody.update(path, JsNumber(123.456)), path))
      }
      "passed a body with invalid consolidated expenses" when {
        List(
          "/ukFhlProperty/expenses/consolidatedExpenses",
          "/ukNonFhlProperty/expenses/consolidatedExpenses"
        ).foreach(path => testValueFormatErrorWith(validBodyConsolidated.update(path, JsNumber(123.456)), path))
      }

      "passed a body with multiple invalid fields" in {
        val path0 = "/ukFhlProperty/expenses/travelCosts"
        val path1 = "/ukNonFhlProperty/expenses/travelCosts"
        val path2 = "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"

        val invalidBody = validBody
          .update(path0, JsNumber(123.456))
          .update(path1, JsNumber(123.456))
          .update(path2, JsNumber(123.456))

        val result: Either[ErrorWrapper, AmendUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validSubmissionId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List(path0, path1, path2))))
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
            Some(List(NinoFormatError, TaxYearFormatError, SubmissionIdFormatError, BusinessIdFormatError))
          )
        )
      }
    }
  }

}
