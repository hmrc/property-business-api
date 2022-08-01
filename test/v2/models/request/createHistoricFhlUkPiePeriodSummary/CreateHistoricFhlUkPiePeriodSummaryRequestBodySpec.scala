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

package v2.models.request.createHistoricFhlUkPiePeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.common.ukFhlPieProperty.{UkFhlPiePropertyExpenses, UkFhlPiePropertyIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

class CreateHistoricFhlUkPiePeriodSummaryRequestBodySpec extends UnitSpec {

  val income:UkFhlPiePropertyIncome =     UkFhlPiePropertyIncome(
      Some(100.25),
      Some(100.15),
      Some(UkPropertyIncomeRentARoom(Some(97.50)))
  )

  val expenses:UkFhlPiePropertyExpenses =  UkFhlPiePropertyExpenses(
      Some(123.12),
      Some(17.90),
      Some(38.19),
      Some(13.42),
      Some(29.42),
      Some(751.00),
      Some(1259.18),
      Some(12.00),
      Some(UkPropertyExpensesRentARoom(Some(12.12))))

  val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(
      "2017-04-06",
      "2017-07-05",
      Some(income),
      Some(expenses)
    )

    val mtdJson: JsValue = Json.parse(
      """
        |{
        |"fromDate" : "2017-04-06",
        | "toDate":    "2017-07-05",
         "income": {
        |    "periodAmount":  100.25,
        |    "taxDeducted":  100.15,
        |    "rentARoom": {
        |      "rentsReceived": 97.50
        |   }
        |},
        | "expenses": {
        |    "premisesRunningCosts": 123.12,
        |    "repairsAndMaintenance": 17.90,
        |    "financialCosts": 38.19,
        |    "professionalFees": 13.42,
        |    "costOfServices": 29.42,
        |    "other": 751.00,
        |    "consolidatedExpenses":1259.18,
        |    "travelCosts": 12.00,
        |    "rentARoom": {
        |     "amountClaimed": 12.12
        |    }
        | }
        | }
        |""".stripMargin
    )

    val desJson: JsValue = Json.parse(
      """
        |{
        |"from" : "2017-04-06",
        | "to":    "2017-07-05",
        | "financials":
        |  {
        |   "incomes": {
        |    "rentIncome": {
        |      "amount": 100.25,
        |      "taxDeducted": 100.15
        |    },
        |    "ukRentARoom": {
        |       "rentsReceived": 97.50
        |    }
        |  },
        |  "deductions": {
        |    "premisesRunningCosts": 123.12,
        |    "repairsAndMaintenance": 17.90,
        |    "financialCosts": 38.19,
        |    "professionalFees": 13.42,
        |    "costOfServices": 29.42,
        |    "other": 751.00,
        |    "consolidatedExpenses":1259.18,
        |    "travelCosts": 12.00,
        |    "ukRentARoom": {
        |       "amountClaimed": 12.12
        |    }
        | }
        |}
        |}
        |""".stripMargin
    )

    "reads" when {
      "passed a valid JSON" should {
        "return a valid model" in {
          mtdJson.as[CreateHistoricFhlUkPiePeriodSummaryRequestBody] shouldBe requestBody
        }
      }
    }

    "writes" when {
      "passed valid model" should {
        "return valid JSON" in {
          Json.toJson(requestBody) shouldBe desJson
        }
      }
    }
}
