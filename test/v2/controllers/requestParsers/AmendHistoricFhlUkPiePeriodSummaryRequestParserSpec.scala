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

package v2.controllers.requestParsers

import api.models.domain.{Nino, PeriodId}
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, RuleBothExpensesSuppliedError}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.mocks.validators.MockAmendHistoricFhlUkPiePeriodSummaryValidator
import v2.models.request.amendHistoricFhlUkPiePeriodSummary.{AmendHistoricFhlUkPiePeriodSummaryRawData, AmendHistoricFhlUkPiePeriodSummaryRequest, AmendHistoricFhlUkPiePeriodSummaryRequestBody}
import v2.models.request.common.ukFhlPieProperty.{UkFhlPieExpenses, UkFhlPieIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

class AmendHistoricFhlUkPiePeriodSummaryRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  private val periodId               = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-123"

  val incomeRentARoom: UkPropertyIncomeRentARoom =
    UkPropertyIncomeRentARoom(Some(5167.56))

  val expensesRentARoom: UkPropertyExpensesRentARoom =
    UkPropertyExpensesRentARoom(Some(945.9))

  val income: UkFhlPieIncome = UkFhlPieIncome(
    Some(1123.45),
    Some(2134.53),
    Some(incomeRentARoom)
  )

  val expenses: UkFhlPieExpenses = UkFhlPieExpenses(
    Some(5167.53),
    Some(424.65),
    Some(853.56),
    Some(835.78),
    Some(978.34),
    Some(382.34),
    None,
    Some(145.56),
    Some(expensesRentARoom)
  )

  val consolidatedExpenses: UkFhlPieExpenses =
    UkFhlPieExpenses(None, None, None, None, None, None, Some(135.78), None, None)

  val requestBody: AmendHistoricFhlUkPiePeriodSummaryRequestBody =
    AmendHistoricFhlUkPiePeriodSummaryRequestBody(
      Some(income),
      Some(expenses)
    )

  val consolidatedRequestBody: AmendHistoricFhlUkPiePeriodSummaryRequestBody =
    AmendHistoricFhlUkPiePeriodSummaryRequestBody(
      Some(income),
      Some(consolidatedExpenses)
    )

  val requestBodyJson: JsValue = Json.parse(
    """
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
      |""".stripMargin
  )

  val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
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
      |""".stripMargin
  )

  val invalidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "repairsAndMaintenance":424.65,
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin
  )

  val inputData: AmendHistoricFhlUkPiePeriodSummaryRawData =
    AmendHistoricFhlUkPiePeriodSummaryRawData(nino, periodId, requestBodyJson)

  val consolidatedInputData: AmendHistoricFhlUkPiePeriodSummaryRawData =
    AmendHistoricFhlUkPiePeriodSummaryRawData(nino, periodId, consolidatedRequestBodyJson)

  val invalidInputData: AmendHistoricFhlUkPiePeriodSummaryRawData =
    AmendHistoricFhlUkPiePeriodSummaryRawData(nino, periodId, invalidRequestBodyJson)

  trait Test extends MockAmendHistoricFhlUkPiePeriodSummaryValidator {
    lazy val parser = new AmendHistoricFhlUkPiePeriodSummaryRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a request object" when {
      "valid unconsolidated request data is supplied" in new Test {
        MockAmendHistoricFhlUkPiePeriodSummaryValidator.validate(inputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPiePeriodSummaryRequest] = parser.parseRequest(inputData)

        result shouldBe Right(AmendHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBody))
      }

      "valid consolidated request data is supplied" in new Test {
        MockAmendHistoricFhlUkPiePeriodSummaryValidator.validate(consolidatedInputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricFhlUkPiePeriodSummaryRequest] = parser.parseRequest(consolidatedInputData)

        result shouldBe Right(AmendHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), consolidatedRequestBody))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendHistoricFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendHistoricFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, RuleBothExpensesSuppliedError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleBothExpensesSuppliedError))))
      }
    }
  }

}
