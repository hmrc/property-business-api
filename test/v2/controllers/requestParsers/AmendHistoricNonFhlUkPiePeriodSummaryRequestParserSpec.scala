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
import v2.models.domain.{ Nino, PeriodId }
import v2.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, RuleBothExpensesSuppliedError }

class AmendHistoricNonFhlUkPiePeriodSummaryRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  private val periodId               = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-123"

  val requestBody: AmendHistoricNonFhlUkPiePeriodSummaryRequestBody =
    AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(
      Some(income),
      Some(expenses)
    )

  val consolidatedRequestBody: AmendHistoricNonFhlUkPiePeriodSummaryRequestBody =
    AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(
      Some(income),
      Some(consolidatedExpenses)
    )

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 4995.99
      |       }
      |   },
      |   "expenses":{
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 4999.99,
      |      "financialCosts": 4998.99,
      |      "professionalFees": 4997.99,
      |      "costOfServices": 4996.99,
      |      "other": 4995.99,
      |      "travelCosts": 4994.99,
      |      "residentialFinancialCostsCarriedForward": 4993.99,
      |      "residentialFinancialCost": 4992.99,
      |      "rentARoom":{
      |         "amountClaimed": 4991.99
      |       }
      |   }
      |}
      |""".stripMargin
  )

  val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 4995.99
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  val invalidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 4995.99
      |       }
      |   },
      |   "expenses":{
      |      "repairsAndMaintenance":424.65,
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin
  )

  val inputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, requestBodyJson)

  val consolidatedInputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, consolidatedRequestBodyJson)

  val invalidInputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, invalidRequestBodyJson)

  trait Test extends MockAmendHistoricNonFhlUkPiePeriodSummaryValidator {
    lazy val parser = new AmendHistoricNonFhlUkPiePeriodSummaryRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a request object" when {
      "valid unconsolidated request data is supplied" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator.validate(inputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPiePeriodSummaryRequest] = parser.parseRequest(inputData)

        result shouldBe Right(AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBody))
      }

      "valid consolidated request data is supplied" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator.validate(consolidatedInputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPiePeriodSummaryRequest] = parser.parseRequest(consolidatedInputData)

        result shouldBe Right(AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), consolidatedRequestBody))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, RuleBothExpensesSuppliedError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleBothExpensesSuppliedError))))
      }
    }
  }
}
