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

package v4.historicFhlUkPropertyPeriodSummary.amend.def1

import common.models.domain.PeriodId
import common.models.errors.{PeriodIdFormatError, RuleBothExpensesSuppliedError}
import config.MockAppConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request._
import v4.historicFhlUkPropertyPeriodSummary.amend.AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory
import v4.historicFhlUkPropertyPeriodSummary.amend.def1.model.request.{UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v4.historicFhlUkPropertyPeriodSummary.amend.request.{
  AmendHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData
}

class Def1_AmendHistoricFhlUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  private val validBody = Json.parse("""
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "premisesRunningCosts":5167.53,
      |      "repairsAndMaintenance":424.65,
      |      "financialCosts":853.56,
      |      "professionalFees":835.78,
      |      "costOfServices":978.34,
      |      "other":382.34,
      |      "travelCosts":145.56,
      |      "rentARoom":{
      |         "amountClaimed":945.9
      |       }
      |   }
      |}
      |""".stripMargin)

  private val validBodyConsolidated = Json.parse("""
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin)

  private val parsedNino     = Nino(validNino)
  private val parsedPeriodId = PeriodId(validPeriodId)

  private val parsedUkPropertyIncomeRentARoom = UkPropertyIncomeRentARoom(Some(5167.56))

  private val parsedUkFhlPieIncome = UkFhlPropertyIncome(Some(1123.45), Some(2134.53), Some(parsedUkPropertyIncomeRentARoom))

  private val parsedUkPropertyExpensesRentARoom = UkPropertyExpensesRentARoom(Some(945.9))

  // @formatter:off
  private val parsedUkFhlPieExpenses = UkFhlPropertyExpenses(
    Some(5167.53), Some(424.65), Some(853.56), Some(835.78),
    Some(978.34), Some(382.34), None, Some(145.56), Some(parsedUkPropertyExpensesRentARoom)
  )


  private val parsedUkFhlPieExpensesConsolidated = UkFhlPropertyExpenses(
    None, None, None, None, None, None, Some(135.78), None, None
  )
  // @formatter:on

  private val parsedBody = Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody(
    Some(parsedUkFhlPieIncome),
    Some(parsedUkFhlPieExpenses)
  )

  private val parsedBodyConsolidated = parsedBody.copy(expenses = Some(parsedUkFhlPieExpensesConsolidated))

  private val validatorFactory = new AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  private def validator(nino: String, periodId: String, body: JsValue) = validatorFactory.validator(nino, periodId, body)

  private def setupMocks(): Unit = {
    MockedAppConfig.minimumTaxYearHistoric.returns(TaxYear.starting(2017)).anyNumberOfTimes()
    MockedAppConfig.maximumTaxYearHistoric.returns(TaxYear.starting(2021)).anyNumberOfTimes()
  }

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedPeriodId, parsedBody))
      }

      "passed a valid consolidated request" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, validBodyConsolidated).validateAndWrapResult()

        result shouldBe Right(Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData(parsedNino, parsedPeriodId, parsedBodyConsolidated))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid nino", validPeriodId, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted periodId start date" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "20A7-04-06_2017-07-04", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed an invalidly formatted periodId end date" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2017-04-06_2017-A7-04", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed an invalidly formatted periodId" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2017-04-06__2017-A7-04", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed a periodId with a non-historic year" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2012-04-06_2012-07-04", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed a periodId where the toDate precedes the fromDate" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, "2019-07-04_2019-04-06", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, PeriodIdFormatError))
      }

      "passed an empty body" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with empty income and expenses sub-objects" in {
        setupMocks()
        val invalidBody = Json.parse("""
            |{
            |   "income":{},
            |   "expenses":{}
            |}
            |""".stripMargin)

        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPaths(List("/income", "/expenses"))))
      }

      "passed a body with an empty rentARoom sub-object" in {
        setupMocks()
        val invalidBody = validBody.replaceWithEmptyObject("/income/rentARoom")

        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/income/rentARoom")))
      }

      "passed a body with multiple invalid numeric amounts" in {
        setupMocks()
        val invalidBody = validBody
          .update("/income/taxDeducted", JsNumber(999999999990.99))
          .update("/income/rentARoom/rentsReceived", JsNumber(-1))

        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPaths(List("/income/taxDeducted", "/income/rentARoom/rentsReceived"))))
      }

      "passed a body with both expenses supplied" in {
        setupMocks()
        val invalidBody = validBody
          .update("/expenses/consolidatedExpenses", JsNumber(222))

        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator(validNino, validPeriodId, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        setupMocks()
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validator("invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, PeriodIdFormatError))
          )
        )
      }
    }
  }

}
