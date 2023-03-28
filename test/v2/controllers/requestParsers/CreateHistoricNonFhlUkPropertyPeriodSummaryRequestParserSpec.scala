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

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import api.models.domain.Nino
import api.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, RuleBothExpensesSuppliedError }
import v2.mocks.validators.MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRawData,
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequest,
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  UkNonFhlPropertyExpenses,
  UkNonFhlPropertyIncome
}

class CreateHistoricNonFhlUkPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  implicit val correlationId: String = "X-123"

  val income: UkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(Some(2355.45), Some(454.56), Some(123.45), Some(234.53), Some(567.89), Some(UkPropertyIncomeRentARoom(Some(567.56))))

  val expenses: UkNonFhlPropertyExpenses = UkNonFhlPropertyExpenses(
    Some(567.53),
    Some(324.65),
    Some(453.56),
    Some(535.78),
    Some(678.34),
    Some(682.34),
    Some(1000.45),
    Some(645.56),
    Some(672.34),
    Some(UkPropertyExpensesRentARoom(Some(545.9))),
    None
  )

  val consolidatedExpenses: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(235.78))

  val createHistoricNonFhlUkPropertyPeriodSummaryRequestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      "2019-03-11",
      "2020-04-23",
      Some(income),
      Some(expenses)
    )

  val createHistoricNonFhlUkPropertyPeriodSummaryConsolidatedRequestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      "2019-03-11",
      "2020-04-23",
      Some(income),
      Some(consolidatedExpenses)
    )

  private val requestBodyJson = Json.parse(
    """
      |{
      | "fromDate": "2019-03-11",
      | "toDate": "2020-04-23", 
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
      |""".stripMargin
  )

  val requestBodyJsonConsolidated: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,  
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}
    """.stripMargin
  )

  val invalidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,  
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "premisesRunningCosts": 567.53,
      |          "consolidatedExpenses": 235.78
      |     }
      |}
    """.stripMargin
  )

  val inputData: CreateHistoricNonFhlUkPropertyPeriodSummaryRawData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino, requestBodyJson)

  val inputDataConsolidated: CreateHistoricNonFhlUkPropertyPeriodSummaryRawData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino, requestBodyJsonConsolidated)

  val invalidInputData: CreateHistoricNonFhlUkPropertyPeriodSummaryRawData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino, invalidRequestBodyJson)

  trait Test extends MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator {
    lazy val parser = new CreateHistoricNonFhlUkPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a request object" when {
      "valid unconsolidated request data is supplied" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), createHistoricNonFhlUkPropertyPeriodSummaryRequestBody))
      }

      "valid consolidated request data is supplied" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator.validate(inputDataConsolidated).returns(Nil)

        parser.parseRequest(inputDataConsolidated) shouldBe
          Right(CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), createHistoricNonFhlUkPropertyPeriodSummaryConsolidatedRequestBody))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, RuleBothExpensesSuppliedError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleBothExpensesSuppliedError))))
      }
    }
  }

}
