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

import api.controllers.validators.Validator
import api.models.domain.Nino
import api.models.errors._
import api.models.utils.JsonErrorValidators
import play.api.libs.json._
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  UkNonFhlPropertyExpenses,
  UkNonFhlPropertyIncome
}

class CreateHistoricNonFhlUkPiePeriodSummaryValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"

  private val validBody = Json.parse("""
      |{
      | "fromDate": "2017-03-11",
      | "toDate": "2018-03-11",
      |   "income": {
      |     "periodAmount": 123.45,
      |     "premiumsOfLeaseGrant": 2355.45,
      |     "reversePremiums": 454.56,
      |     "otherIncome": 567.89,
      |     "taxDeducted": 234.53,
      |     "rentARoom": {
      |       "rentsReceived": 567.56
      |     }
      |   },
      |  "expenses": {
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin)

  private val validBodyConsolidated =
    validBody.replaceWithEmptyObject("/expenses").update("/expenses", Json.obj("consolidatedExpenses" -> JsNumber(111.25)))

  private val parsedNino = Nino(validNino)

  private val parsedUkPropertyIncomeRentARoom = UkPropertyIncomeRentARoom(Some(567.56))

  private val parsedUkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(Some(2355.45), Some(454.56), Some(123.45), Some(234.53), Some(567.89), Some(parsedUkPropertyIncomeRentARoom))

  private val parsedUkPropertyExpensesRentARoom = UkPropertyExpensesRentARoom(Some(545.9))

  //@formatter:off
  private val parsedUkNonFhlPropertyExpenses = UkNonFhlPropertyExpenses(
    Some(567.53), Some(324.65), Some(453.56), Some(535.78), Some(678.34),
    Some(682.34), Some(1000.45), Some(645.56), Some(672.34),
    Some(parsedUkPropertyExpensesRentARoom), None
  )

  private val parsedUkNonFhlPropertyExpensesConsolidated = UkNonFhlPropertyExpenses(
    None, None, None, None, None, None, None, None, None, None, Some(111.25) 
  )
  //@formatter:on

  private val parsedBody = CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
    "2017-03-11",
    "2018-03-11",
    Some(parsedUkNonFhlPropertyIncome),
    Some(parsedUkNonFhlPropertyExpenses)
  )

  private val parsedBodyConsolidated = parsedBody.copy(expenses = Some(parsedUkNonFhlPropertyExpensesConsolidated))

  private val validatorFactory = new CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory()

  private def validator(nino: String, body: JsValue): Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    validatorFactory.validator(nino, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBody).validateAndWrapResult()

        result shouldBe Right(CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedBody))
      }

      "passed a valid consolidated request" in {
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedBodyConsolidated))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed a body with a missing mandatory field" in {
        val invalidBody = validBody.removeProperty("/fromDate")
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/fromDate")))
      }

      "passed a body containing only a fromDate and toDate" in {
        val invalidBody = validBody.removeProperty("/income").removeProperty("/expenses")
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body containing multiple fields with invalid numeric amounts" in {
        val invalidBody = validBodyConsolidated
          .update("/income/periodAmount", JsNumber(-1.00))
          .update("/income/premiumsOfLeaseGrant", JsNumber(999999999990.99))
          .update("/expenses/consolidatedExpenses", JsNumber(235.781))

        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List("/income/periodAmount", "/income/premiumsOfLeaseGrant", "/expenses/consolidatedExpenses"))))
      }

      "passed a body with an invalidly formatted fromDate" in {
        val invalidBody = validBody.update("fromDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "passed a body with a fromDate that precedes the minimum" in {
        val invalidBody = validBody.update("fromDate", JsString("1801-12-17"))
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateOutOfRangeError))
      }

      "passed a body with an invalidly formatted toDate" in {
        val invalidBody = validBody.update("toDate", JsString("invalid"))
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "passed a body with a toDate that proceeds the maximum" in {
        val invalidBody = validBody.update("toDate", JsString("2104-01-31"))
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateOutOfRangeError))
      }

      "passed a body where the toDate precedes the fromDate" in {
        val invalidBody = validBody
          .update("fromDate", JsString("2020-04-23"))
          .update("toDate", JsString("2019-03-11"))
        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }

      "passed a body containing both expenses" in {
        val invalidBody = validBody.update("/expenses/consolidatedExpenses", JsNumber(121.11))

        val result: Either[ErrorWrapper, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses")))
      }
    }
  }

}
