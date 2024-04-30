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

package v4.amendHistoricNonFhlUkPropertyPeriodSummary.def1.model.request

import play.api.libs.json.{JsValue, Json}
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.model.request.Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody

trait Def1_Fixtures {

  val downstreamJsonIncome: JsValue = Json.parse("""
    |{
    |    "rentIncome": {
    |        "amount": 100.00,
    |        "taxDeducted": 500.00
    |    },
    |    "premiumsOfLeaseGrant": 200.00,
    |    "reversePremiums": 300.00,
    |    "otherIncome": 400.00,
    |    "ukRentARoom": {
    |        "rentsReceived": 600.00
    |    }
    |}
    |""".stripMargin)

  val mtdJsonRequestWithEmptySubObjects: JsValue = Json.parse(
    """
      |{
      |   "income":{},
      |   "expenses":{}
      |}
      |""".stripMargin
  )

  val mtdJsonRequestWithEmptyRentARoom: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{}
      |   },
      |   "expenses":{
      |      "consolidatedExpenses": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  val mtdJsonIncome: JsValue = Json.parse("""
    |{
    |    "periodAmount": 100.00,
    |    "premiumsOfLeaseGrant": 200.00,
    |    "reversePremiums": 300.00,
    |    "otherIncome": 400.00,
    |    "taxDeducted": 500.00,
    |    "rentARoom": {
    |        "rentsReceived": 600.00
    |    }
    |}
    |""".stripMargin)

  val mtdJsonExpensesFull: JsValue = Json.parse("""
    |{
    |    "premisesRunningCosts": 100.00,
    |    "repairsAndMaintenance": 200.00,
    |    "financialCosts": 300.00,
    |    "professionalFees": 400.00,
    |    "costOfServices": 500.00,
    |    "other": 600.00,
    |    "travelCosts": 700.00,
    |    "residentialFinancialCostsCarriedForward": 800.00,
    |    "residentialFinancialCost": 900.00,
    |    "rentARoom": {
    |        "amountClaimed": 1000.00
    |    }
    |}
    |""".stripMargin)

  val mtdJsonExpensesConsolidated: JsValue = Json.parse("""
    |{
    |    "consolidatedExpenses": 100.00
    |}
    |""".stripMargin)

  val mtdJsonRequestConsolidated: JsValue = Json.parse(s"""
    |{
    |    "income": $mtdJsonIncome,
    |    "expenses": $mtdJsonExpensesConsolidated
    |}
    |""".stripMargin)

  val mtdJsonRequestFull: JsValue = Json.parse(s"""
    |{
    |    "income": $mtdJsonIncome,
    |    "expenses": $mtdJsonExpensesFull
    |}
    |""".stripMargin)

  val downstreamJsonExpensesFull: JsValue = Json.parse("""
    |{
    |    "premisesRunningCosts": 100.00,
    |    "repairsAndMaintenance": 200.00,
    |    "financialCosts": 300.00,
    |    "professionalFees": 400.00,
    |    "costOfServices": 500.00,
    |    "other": 600.00,
    |    "travelCosts": 700.00,
    |    "residentialFinancialCostsCarriedForward": 800.00,
    |    "residentialFinancialCost": 900.00,
    |    "ukRentARoom": {
    |        "amountClaimed": 1000.00
    |    }
    |}
    |""".stripMargin)

  val downstreamJsonRequestFull: JsValue = Json.parse(s"""
    |{
    |    "incomes": $downstreamJsonIncome,
    |    "deductions": $downstreamJsonExpensesFull
    |}
    |""".stripMargin)

  val downstreamJsonExpensesConsolidated: JsValue = Json.parse("""
    |{
    |    "consolidatedExpenses": 100.00
    |}
    |""".stripMargin)

  val downstreamJsonRequestConsolidated: JsValue = Json.parse(s"""
    |{
    |    "incomes": $downstreamJsonIncome,
    |    "deductions": $downstreamJsonExpensesConsolidated
    |}
    |""".stripMargin)

  val rentARoomExpenses: UkPropertyExpensesRentARoom = UkPropertyExpensesRentARoom(Some(1000.00))
  val rentARoomIncome: UkPropertyIncomeRentARoom     = UkPropertyIncomeRentARoom(Some(600.00))

  val ukNonFhlPropertyIncome: UkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(
      periodAmount = Some(100.00),
      premiumsOfLeaseGrant = Some(200.00),
      reversePremiums = Some(300.00),
      otherIncome = Some(400.00),
      taxDeducted = Some(500.00),
      rentARoom = Some(rentARoomIncome)
    )

  val ukNonFhlPieExpensesFull: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(
      premisesRunningCosts = Some(100.00),
      repairsAndMaintenance = Some(200.00),
      financialCosts = Some(300.00),
      professionalFees = Some(400.00),
      costOfServices = Some(500.00),
      other = Some(600.00),
      consolidatedExpenses = None,
      travelCosts = Some(700.00),
      residentialFinancialCostsCarriedForward = Some(800.00),
      residentialFinancialCost = Some(900.00),
      rentARoom = Some(rentARoomExpenses)
    )

  val ukNonFhlPieExpensesConsolidated: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(
      premisesRunningCosts = None,
      repairsAndMaintenance = None,
      financialCosts = None,
      professionalFees = None,
      costOfServices = None,
      other = None,
      consolidatedExpenses = Some(100.00),
      travelCosts = None,
      residentialFinancialCostsCarriedForward = None,
      residentialFinancialCost = None,
      rentARoom = None
    )

  val ukNonFhlPropertyExpensesFull: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(
      premisesRunningCosts = Some(100.00),
      repairsAndMaintenance = Some(200.00),
      financialCosts = Some(300.00),
      professionalFees = Some(400.00),
      costOfServices = Some(500.00),
      other = Some(600.00),
      consolidatedExpenses = None,
      travelCosts = Some(700.00),
      residentialFinancialCostsCarriedForward = Some(800.00),
      residentialFinancialCost = Some(900.00),
      rentARoom = Some(rentARoomExpenses)
    )

  val ukNonFhlPropertyExpensesConsolidated: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(
      premisesRunningCosts = None,
      repairsAndMaintenance = None,
      financialCosts = None,
      professionalFees = None,
      costOfServices = None,
      other = None,
      consolidatedExpenses = Some(100.00),
      travelCosts = None,
      residentialFinancialCostsCarriedForward = None,
      residentialFinancialCost = None,
      rentARoom = None
    )

  val requestBodyFull: Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(income = Some(ukNonFhlPropertyIncome), expenses = Some(ukNonFhlPieExpensesFull))

  val requestBodyConsolidated: Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      income = Some(ukNonFhlPropertyIncome),
      expenses = Some(ukNonFhlPieExpensesConsolidated))

}
