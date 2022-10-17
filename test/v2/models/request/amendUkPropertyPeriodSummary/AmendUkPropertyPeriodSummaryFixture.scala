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

package v2.models.request.amendUkPropertyPeriodSummary

import play.api.libs.json.{ JsValue, Json }
import v2.models.request.amendUkPropertyPeriodSummary.amendUkFhlProperty.{ AmendUkFhlProperty, AmendUkFhlPropertyExpenses }
import v2.models.request.amendUkPropertyPeriodSummary.amendUkNonFhlProperty.{ AmendUkNonFhlProperty, AmendUkNonFhlPropertyExpenses }
import v2.models.request.common.ukFhlProperty.UkFhlPropertyIncome
import v2.models.request.common.ukNonFhlProperty.UkNonFhlPropertyIncome
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }

object AmendUkPropertyPeriodSummaryFixture {

  val amendUkFhlPropertyIncome: UkFhlPropertyIncome = UkFhlPropertyIncome(
    periodAmount = Some(1234.12),
    taxDeducted = Some(1234.23),
    rentARoom = Some(
      UkPropertyIncomeRentARoom(
        Some(1234.34)
      ))
  )

  val amendUkFhlPropertyExpenses: AmendUkFhlPropertyExpenses = AmendUkFhlPropertyExpenses(
    premisesRunningCosts = Some(1234.45),
    repairsAndMaintenance = Some(1234.56),
    financialCosts = Some(1234.67),
    professionalFees = Some(1234.78),
    costOfServices = Some(1234.89),
    other = Some(1234.12),
    consolidatedExpenses = None,
    travelCosts = Some(1234.23),
    rentARoom = Some(
      UkPropertyExpensesRentARoom(
        Some(1234.34)
      ))
  )

  val amendUkFhlPropertyConsolidatedExpenses: AmendUkFhlPropertyExpenses =
    AmendUkFhlPropertyExpenses(
      premisesRunningCosts = None,
      repairsAndMaintenance = None,
      financialCosts = None,
      professionalFees = None,
      costOfServices = None,
      other = None,
      consolidatedExpenses = Some(80.55),
      travelCosts = None,
      rentARoom = None
    )

  val amendUkNonFhlPropertyIncome: UkNonFhlPropertyIncome = UkNonFhlPropertyIncome(
    premiumsOfLeaseGrant = Some(9876.12),
    reversePremiums = Some(9876.23),
    periodAmount = Some(9876.34),
    taxDeducted = Some(9876.45),
    otherIncome = Some(9876.56),
    rentARoom = Some(
      UkPropertyIncomeRentARoom(
        Some(9876.67)
      ))
  )

  val amendUkNonFhlPropertyExpenses: AmendUkNonFhlPropertyExpenses = AmendUkNonFhlPropertyExpenses(
    premisesRunningCosts = Some(9876.78),
    repairsAndMaintenance = Some(9876.89),
    financialCosts = Some(9876.12),
    professionalFees = Some(9876.23),
    costOfServices = Some(9876.34),
    other = Some(9876.45),
    residentialFinancialCost = Some(9876.56),
    travelCosts = Some(9876.67),
    residentialFinancialCostsCarriedForward = Some(9876.78),
    rentARoom = Some(
      UkPropertyExpensesRentARoom(
        Some(9876.89)
      )),
    consolidatedExpenses = None
  )

  val amendUkNonFhlPropertyConsolidatedExpenses: AmendUkNonFhlPropertyExpenses = AmendUkNonFhlPropertyExpenses(
    premisesRunningCosts = None,
    repairsAndMaintenance = None,
    financialCosts = None,
    professionalFees = None,
    costOfServices = None,
    other = None,
    residentialFinancialCost = None,
    travelCosts = None,
    residentialFinancialCostsCarriedForward = None,
    rentARoom = None,
    consolidatedExpenses = Some(80.55)
  )

  val amendUkFhlProperty: AmendUkFhlProperty = AmendUkFhlProperty(
    Some(amendUkFhlPropertyIncome),
    Some(amendUkFhlPropertyExpenses)
  )

  val amendUkNonFhlProperty: AmendUkNonFhlProperty = AmendUkNonFhlProperty(
    Some(amendUkNonFhlPropertyIncome),
    Some(amendUkNonFhlPropertyExpenses)
  )

  val amendUkPropertyPeriodSummaryRequestBody: AmendUkPropertyPeriodSummaryRequestBody = {
    AmendUkPropertyPeriodSummaryRequestBody(
      ukFhlProperty = Some(amendUkFhlProperty),
      ukNonFhlProperty = Some(amendUkNonFhlProperty)
    )

  }

  val amendUkPropertyPeriodSummaryRequestConsolidatedBody: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(
    ukFhlProperty = Some(AmendUkFhlProperty(Some(amendUkFhlPropertyIncome), Some(amendUkFhlPropertyConsolidatedExpenses))),
    ukNonFhlProperty = Some(AmendUkNonFhlProperty(Some(amendUkNonFhlPropertyIncome), Some(amendUkNonFhlPropertyConsolidatedExpenses)))
  )

  val mtdAmendUkFhlPropertyIncomeJson: JsValue = Json.parse("""
    |{
    |   "periodAmount": 1234.12,
    |   "taxDeducted": 1234.23,
    |   "rentARoom": {
    |     "rentsReceived": 1234.34
    |   }
    | }
    |""".stripMargin)

  val mtdAmendUkFhlPropertyExpensesJson: JsValue = Json.parse("""
     |{
     |   "premisesRunningCosts": 1234.45,
     |   "repairsAndMaintenance": 1234.56,
     |   "financialCosts": 1234.67,
     |   "professionalFees": 1234.78,
     |   "costOfServices": 1234.89,
     |   "other": 1234.12,
     |   "travelCosts": 1234.23,
     |   "rentARoom" : {
     |      "amountClaimed": 1234.34
     |   }
     |}
     | """.stripMargin)

  val mtdAmendUkNonFhlPropertyIncomeJson: JsValue = Json.parse("""
     |{
     |  "premiumsOfLeaseGrant": 9876.12,
     |  "reversePremiums": 9876.23,
     |  "periodAmount": 9876.34,
     |  "taxDeducted": 9876.45,
     |  "otherIncome": 9876.56,
     |  "rentARoom": {
     |      "rentsReceived": 9876.67
     |   }
     |}
     |""".stripMargin)

  val mtdAmendUkNonFhlPropertyExpensesJson: JsValue = Json.parse("""
     |{
     |  "premisesRunningCosts": 9876.78,
     |  "repairsAndMaintenance": 9876.89,
     |  "financialCosts": 9876.12,
     |  "professionalFees": 9876.23,
     |  "costOfServices": 9876.34,
     |  "other": 9876.45,
     |  "residentialFinancialCost": 9876.56,
     |  "travelCosts": 9876.67,
     |  "residentialFinancialCostsCarriedForward": 9876.78,
     |  "rentARoom": {
     |    "amountClaimed": 9876.89
     |   }
     |}
     | """.stripMargin)

  val mtdConsolidatedExpensesJson: JsValue = Json.parse("""
     |{
     |  "consolidatedExpenses": 80.55
     |}
     |""".stripMargin)

  val mtdAmendUkFhlPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income": $mtdAmendUkFhlPropertyIncomeJson,
     |  "expenses": $mtdAmendUkFhlPropertyExpensesJson
     |}""".stripMargin)

  val mtdAmendUkNonFhlPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income":$mtdAmendUkNonFhlPropertyIncomeJson,
     |  "expenses": $mtdAmendUkNonFhlPropertyExpensesJson
     |}""".stripMargin)

  val mtdUkNonFhlConsolidatedPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income":$mtdAmendUkNonFhlPropertyIncomeJson,
     |  "expenses": $mtdConsolidatedExpensesJson
     |}""".stripMargin)

  val mtdUkFhlConsolidatedPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income": $mtdAmendUkFhlPropertyIncomeJson,
     |  "expenses": $mtdConsolidatedExpensesJson
     |}
     |""".stripMargin)

  val mtdConsolidatedRequestJson: JsValue = Json.parse(s"""
     |{
     |  "ukFhlProperty":$mtdUkFhlConsolidatedPropertyJson,
     |  "ukNonFhlProperty": $mtdUkNonFhlConsolidatedPropertyJson
     |}
     |""".stripMargin)

  val mtdNonConsolidatedRequestJson: JsValue = Json.parse(s"""
     |{
     |  "ukFhlProperty": $mtdAmendUkFhlPropertyJson,
     |  "ukNonFhlProperty": $mtdAmendUkNonFhlPropertyJson
     |}
     |""".stripMargin)

  val downstreamAmendUkFhlPropertyIncomeJson: JsValue = Json.parse("""
     |{
     |  "periodAmount": 1234.12,
     |  "taxDeducted": 1234.23,
     |  "ukFhlRentARoom": {
     |     "rentsReceived": 1234.34
     |   }
     |}
     |""".stripMargin)

  val downstreamAmendUkFhlPropertyExpensesJson: JsValue =
    Json.parse("""
     |{
     |  "premisesRunningCosts": 1234.45,
     |  "financialCosts": 1234.67,
     |  "professionalFees": 1234.78,
     |  "other": 1234.12,
     |  "travelCosts": 1234.23,
     |  "ukFhlRentARoom" : {
     |     "amountClaimed": 1234.34
     |   }
     |}
     |""".stripMargin)

  val downstreamAmendUkNonFhlPropertyIncomeJson: JsValue = Json.parse("""
     |{
     |   "premiumsOfLeaseGrant": 9876.12,
     |   "reversePremiums": 9876.23,
     |   "periodAmount": 9876.34,
     |   "taxDeducted": 9876.45,
     |   "otherIncome": 9876.56,
     |   "ukOtherRentARoom": {
     |      "rentsReceived": 9876.67
     |    }
     |}
     |""".stripMargin)

  val downstreamAmendUkNonFhlPropertyExpensesJson: JsValue = Json.parse("""
     |{
     |   "premisesRunningCosts": 9876.78,
     |   "repairsAndMaintenance": 9876.89,
     |   "financialCosts": 9876.12,
     |   "professionalFees": 9876.23,
     |   "costOfServices": 9876.34,
     |   "other": 9876.45,
     |   "residentialFinancialCost": 9876.56,
     |   "travelCosts": 9876.67,
     |   "residentialFinancialCostsCarriedForward": 9876.78,
     |   "ukOtherRentARoom": {
     |       "amountClaimed": 9876.89
     |    }
     |}
     | """.stripMargin)

  val downstreamConsolidatedExpensesJson: JsValue = Json.parse("""
     |{
     |  "consolidatedExpense": 80.55
     |}
     |""".stripMargin)

  val downstreamAmendUkFhlPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income": $downstreamAmendUkFhlPropertyIncomeJson,
     |  "expenses": $downstreamAmendUkFhlPropertyExpensesJson
     |}""".stripMargin)

  val downstreamAmendUkNonFhlPropertyJson: JsValue = Json.parse(s"""
     |{
     |  "income": $downstreamAmendUkNonFhlPropertyIncomeJson,
     |  "expenses": $downstreamAmendUkNonFhlPropertyExpensesJson
     |}""".stripMargin)

  val downstreamRequestBodyJson: JsValue                     = Json.parse(s"""
     |{
     |  "ukFhlProperty": $downstreamAmendUkFhlPropertyJson,
     |  "ukOtherProperty": $downstreamAmendUkNonFhlPropertyJson
     |}
     |""".stripMargin)
  val downstreamConsolidatedExpensesRequestBodyJson: JsValue = Json.parse(s"""
     |{
     |  "ukFhlProperty": {
     |    "income": $downstreamAmendUkFhlPropertyIncomeJson,
     |    "expenses": $downstreamConsolidatedExpensesJson
     |  },
     |  "ukOtherProperty": {
     |    "income": $downstreamAmendUkNonFhlPropertyIncomeJson,
     |    "expenses": $downstreamConsolidatedExpensesJson
     |  }
     |}
     |""".stripMargin)

}
