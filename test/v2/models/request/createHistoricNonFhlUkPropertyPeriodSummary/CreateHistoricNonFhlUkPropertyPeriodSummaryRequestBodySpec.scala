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

package v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }

class CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec {

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

  val mtdJson: JsValue = Json.parse(
    """
      {
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
    """.stripMargin
  )

  val mtdJsonConsolidated: JsValue = Json.parse(
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

  val backendJson: JsValue = Json.parse(
    """
      |{
      | "from": "2019-03-11",
      | "to": "2020-04-23",
      | "financials": {
      |   "incomes": {
      |    "rentIncome":{
      |      "amount": 123.45,
      |      "taxDeducted": 234.53
      |    },
      |    "premiumsOfLeaseGrant": 2355.45,
      |    "reversePremiums": 454.56,
      |    "otherIncome": 567.89,
      |    "ukRentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "deductions":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "ukRentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |}
    """.stripMargin
  )

  val backendJsonConsolidated: JsValue = Json.parse(
    """
      |{
      |    "from": "2019-03-11",
      |    "to": "2020-04-23",
      |    "financials":
      |    {
      |      "incomes": {
      |       "rentIncome":{
      |         "amount": 123.45,
      |         "taxDeducted": 234.53
      |       },
      |       "premiumsOfLeaseGrant": 2355.45,
      |       "reversePremiums": 454.56,
      |       "otherIncome": 567.89,
      |       "ukRentARoom": {
      |         "rentsReceived": 567.56
      |       }
      |     },
      |     "deductions":{
      |       "consolidatedExpenses": 235.78
      |     }
      |   }
      |}
    """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] shouldBe createHistoricNonFhlUkPropertyPeriodSummaryRequestBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(createHistoricNonFhlUkPropertyPeriodSummaryRequestBody) shouldBe backendJson
      }
    }
  }

  "reads" when {
    "passed a valid consolidated JSON" should {
      "return a valid model" in {
        mtdJsonConsolidated
          .as[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] shouldBe createHistoricNonFhlUkPropertyPeriodSummaryConsolidatedRequestBody
      }
    }
  }
  "writes" when {
    "passed valid consolidated model" should {
      "return valid consolidated JSON" in {
        Json.toJson(createHistoricNonFhlUkPropertyPeriodSummaryConsolidatedRequestBody) shouldBe backendJsonConsolidated
      }
    }
  }
}
