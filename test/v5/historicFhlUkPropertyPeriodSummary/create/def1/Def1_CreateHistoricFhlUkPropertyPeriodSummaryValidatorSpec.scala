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

package v5.historicFhlUkPropertyPeriodSummary.create.def1

import common.models.errors.{RuleBothExpensesSuppliedError, RuleToDateBeforeFromDateError}
import play.api.libs.json._
import shared.controllers.validators.Validator
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v5.historicFhlUkPropertyPeriodSummary.create.CreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory
import v5.historicFhlUkPropertyPeriodSummary.create.def1.model.request.{UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v5.historicFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody,
  Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData
}

class Def1_CreateHistoricFhlUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"

  private val validBody = Json.parse("""
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 101.25,
      |    "rentARoom": {
      |      "rentsReceived": 102.25
      |    }
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 103.25,
      |    "repairsAndMaintenance": 104.25,
      |    "financialCosts": 105.25,
      |    "professionalFees": 106.25,
      |    "costOfServices": 107.25,
      |    "other": 108.25,
      |    "travelCosts": 109.25,
      |    "rentARoom": {
      |      "amountClaimed": 110.25
      |    }
      |  }
      |}
      |""".stripMargin)

  private val validBodyConsolidated =
    validBody.replaceWithEmptyObject("/expenses").update("/expenses", Json.obj("consolidatedExpenses" -> JsNumber(111.25)))

  private val parsedNino = Nino(validNino)

  private val parsedUkPropertyIncomeRentARoom = UkPropertyIncomeRentARoom(Some(102.25))
  private val parsedUkFhlPieIncome            = UkFhlPropertyIncome(Some(100.25), Some(101.25), Some(parsedUkPropertyIncomeRentARoom))

  private val parsedUkPropertyExpensesRentARoom = UkPropertyExpensesRentARoom(Some(110.25))
  //@formatter:off
  private val parsedUkFhlPieExpenses = UkFhlPropertyExpenses(
     Some(103.25), Some(104.25), Some(105.25), Some(106.25),
     Some(107.25), Some(108.25), None, Some(109.25), Some(parsedUkPropertyExpensesRentARoom)
  )

  private val parsedUkFhlPieExpensesConsolidated = UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(111.25), None, None
  )
  //@formatter:on

  private val parsedBody = Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody(
    "2017-04-06",
    "2017-07-05",
    Some(parsedUkFhlPieIncome),
    Some(parsedUkFhlPieExpenses)
  )

  private val parsedBodyConsolidated = parsedBody.copy(expenses = Some(parsedUkFhlPieExpensesConsolidated))

  private val validatorFactory = new CreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory

  private def validator(nino: String, body: JsValue): Validator[CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
    validatorFactory.validator(nino, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedBody))
      }

      "passed a valid consolidated request" in {
        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedBodyConsolidated))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed a body with a mandatory field missing" in {
        val invalidBody = validBody.removeProperty("/fromDate")

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/fromDate")))
      }

      "passed a body containing only a fromDate and toDate" in {
        val invalidBody = validBody.removeProperty("/income").removeProperty("/expenses")

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body containing multiple invalid numeric amounts" in {
        val invalidBody = validBodyConsolidated
          .update("/income/periodAmount", JsNumber(9999999999999999999.25))
          .update("/income/taxDeducted", JsNumber(-100.25))
          .update("/expenses/consolidatedExpenses", JsNumber(-20000.72))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List("/income/periodAmount", "/income/taxDeducted", "/expenses/consolidatedExpenses"))))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed an invalid fromDate" in {
        val invalidBody = validBody.update("fromDate", JsString("BAD_DATE"))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a fromDate that precedes the minimum" in {
        val invalidBody = validBody.update("fromDate", JsString("1800-01-01"))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed an invalid toDate" in {
        val invalidBody = validBody.update("toDate", JsString("BAD_DATE"))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a toDate that proceeds the maximum" in {
        val invalidBody = validBody.update("toDate", JsString("2101-01-01"))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a toDate that precedes the fromDate" in {
        val invalidBody = validBody.update("fromDate", JsString("2017-07-05")).update("toDate", JsString("2017-04-06"))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body containing both expenses" in {
        val invalidBody = validBody.update("/expenses/consolidatedExpenses", JsNumber(121.11))

        val result: Either[ErrorWrapper, CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/expenses")))
      }
    }
  }

}
