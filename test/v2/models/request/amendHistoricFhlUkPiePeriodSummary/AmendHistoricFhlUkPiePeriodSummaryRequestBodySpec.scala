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

package v2.models.request.amendHistoricFhlUkPiePeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.request.common.ukFhlPieProperty.{UkFhlPieExpenses, UkFhlPieIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

class AmendHistoricFhlUkPiePeriodSummaryRequestBodySpec extends UnitSpec {

  val income: UkFhlPieIncome = UkFhlPieIncome(
    Some(100.25),
    Some(100.15),
    Some(UkPropertyIncomeRentARoom(Some(97.50)))
  )

  val expenses: UkFhlPieExpenses = UkFhlPieExpenses(
    premisesRunningCosts = Some(123.12),
    repairsAndMaintenance = Some(17.90),
    financialCosts = Some(38.19),
    professionalFees = Some(13.42),
    costOfServices = Some(29.42),
    other = Some(751.00),
    consolidatedExpenses = Some(1259.18),
    travelCosts = Some(12.00),
    rentARoom = Some(UkPropertyExpensesRentARoom(Some(12.12)))
  )

  val requestBody: AmendHistoricFhlUkPiePeriodSummaryRequestBody =
    AmendHistoricFhlUkPiePeriodSummaryRequestBody(Some(income), Some(expenses))

  val mtdJson: JsValue = Json.parse(
    """
      |{    
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
      |""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        mtdJson.as[AmendHistoricFhlUkPiePeriodSummaryRequestBody] shouldBe requestBody
      }
    }

    "writes" when {
      "passes a valid model" should {
        "return valid JSON" in {
          Json.toJson(requestBody) shouldBe desJson
        }
      }
    }
  }

}
