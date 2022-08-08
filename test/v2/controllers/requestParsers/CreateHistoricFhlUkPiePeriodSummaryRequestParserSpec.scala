/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.mocks.validators.MockCreateHistoricFhlUkPiePeriodSummaryValidator
import v2.models.domain.Nino
import v2.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, RuleBothExpensesSuppliedError }
import v2.models.request.common.ukFhlPieProperty.{ UkFhlPieExpenses, UkFhlPieIncome }
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{
  CreateHistoricFhlUkPiePeriodSummaryRawData,
  CreateHistoricFhlUkPiePeriodSummaryRequest,
  CreateHistoricFhlUkPiePeriodSummaryRequestBody
}

class CreateHistoricFhlUkPiePeriodSummaryParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  implicit val correlationId: String = "X-123"

  val income: UkFhlPieIncome =
    UkFhlPieIncome(Some(100.25), Some(100.25), Some(UkPropertyIncomeRentARoom(Some(100.25))))

  val expenses: UkFhlPieExpenses = UkFhlPieExpenses(
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    None,
    Some(100.25),
    Some(UkPropertyExpensesRentARoom(Some(100.25)))
  )

  val consolidatedExpenses: UkFhlPieExpenses =
    UkFhlPieExpenses(None, None, None, None, None, None, Some(100.25), None, None)

  val createHistoricFhlUkPiePeriodSummaryRequestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(
      "2017-04-06",
      "2017-07-05",
      Some(income),
      Some(expenses)
    )

  val createHistoricFhlUkPiePeriodSummaryConsolidatedRequestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(
      "2017-04-06",
      "2017-07-05",
      Some(income),
      Some(consolidatedExpenses)
    )

  private val requestBodyJson = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 100.25,
      |    "repairsAndMaintenance": 100.25,
      |    "financialCosts": 100.25,
      |    "professionalFees": 100.25,
      |    "costOfServices": 100.25,
      |    "travelCosts": 100.25,
      |    "other": 100.25,
      |    "rentARoom": {
      |      "amountClaimed": 100.25
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val requestBodyJsonConsolidated: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": 100.25
      |  }
      |}
    """.stripMargin
  )

  val invalidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "repairsAndMaintenance": 100.25,
      |    "consolidatedExpenses": 100.25
      |  }
      |}
    """.stripMargin
  )

  val inputData: CreateHistoricFhlUkPiePeriodSummaryRawData =
    CreateHistoricFhlUkPiePeriodSummaryRawData(nino, requestBodyJson)

  val inputDataConsolidated: CreateHistoricFhlUkPiePeriodSummaryRawData =
    CreateHistoricFhlUkPiePeriodSummaryRawData(nino, requestBodyJsonConsolidated)

  val invalidInputData: CreateHistoricFhlUkPiePeriodSummaryRawData =
    CreateHistoricFhlUkPiePeriodSummaryRawData(nino, invalidRequestBodyJson)

  trait Test extends MockCreateHistoricFhlUkPiePeriodSummaryValidator {
    lazy val parser = new CreateHistoricFhlUkPiePeriodSummaryRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a request object" when {
      "valid unconsolidated request data is supplied" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), createHistoricFhlUkPiePeriodSummaryRequestBody))
      }

      "valid consolidated request data is supplied" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryValidator.validate(inputDataConsolidated).returns(Nil)

        parser.parseRequest(inputDataConsolidated) shouldBe
          Right(CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), createHistoricFhlUkPiePeriodSummaryConsolidatedRequestBody))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateHistoricFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, RuleBothExpensesSuppliedError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleBothExpensesSuppliedError))))
      }
    }
  }

}
