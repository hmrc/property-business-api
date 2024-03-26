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

package v4.controllers.amendUkPropertyPeriodSummary.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers._
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits._
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.{Def1_Amend_UkFhlProperty, Def1_Amend_UkFhlPropertyExpenses}
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty.{
  Def1_Amend_UkNonFhlProperty,
  Def1_Amend_UkNonFhlPropertyExpenses
}
import v4.controllers.amendUkPropertyPeriodSummary.model.request.Def1_AmendUkPropertyPeriodSummaryRequestData

class Def1_AmendUkPropertyPeriodSummaryRulesValidator extends RulesValidator[Def1_AmendUkPropertyPeriodSummaryRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(
      parsed: Def1_AmendUkPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], Def1_AmendUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._
    combine(
      ukFhlProperty.map(validateUkFhlProperty).getOrElse(valid),
      ukNonFhlProperty.map(validateUkNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateUkFhlProperty(ukFhlProperty: Def1_Amend_UkFhlProperty): Validated[Seq[MtdError], Unit] = {

    val valuesWithPaths = List(
      (ukFhlProperty.income.flatMap(_.periodAmount), "/ukFhlProperty/income/periodAmount"),
      (ukFhlProperty.income.flatMap(_.taxDeducted), "/ukFhlProperty/income/taxDeducted"),
      (ukFhlProperty.income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukFhlProperty/income/rentARoom/rentsReceived"),
      (ukFhlProperty.expenses.flatMap(_.premisesRunningCosts), "/ukFhlProperty/expenses/premisesRunningCosts"),
      (ukFhlProperty.expenses.flatMap(_.repairsAndMaintenance), "/ukFhlProperty/expenses/repairsAndMaintenance"),
      (ukFhlProperty.expenses.flatMap(_.financialCosts), "/ukFhlProperty/expenses/financialCosts"),
      (ukFhlProperty.expenses.flatMap(_.professionalFees), "/ukFhlProperty/expenses/professionalFees"),
      (ukFhlProperty.expenses.flatMap(_.costOfServices), "/ukFhlProperty/expenses/costOfServices"),
      (ukFhlProperty.expenses.flatMap(_.other), "/ukFhlProperty/expenses/other"),
      (ukFhlProperty.expenses.flatMap(_.consolidatedExpenses), "/ukFhlProperty/expenses/consolidatedExpenses"),
      (ukFhlProperty.expenses.flatMap(_.travelCosts), "/ukFhlProperty/expenses/travelCosts"),
      (ukFhlProperty.expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = ukFhlProperty.expenses
      .map(_.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          ukFhlProperty.expenses match {
            case Some(Def1_Amend_UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None)) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses")))
          }
      })
      .getOrElse(valid)

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

  private def validateUkNonFhlProperty(ukNonFhlProperty: Def1_Amend_UkNonFhlProperty): Validated[Seq[MtdError], Unit] = {

    val valuesWithPaths = List(
      (ukNonFhlProperty.income.flatMap(_.premiumsOfLeaseGrant), "/ukNonFhlProperty/income/premiumsOfLeaseGrant"),
      (ukNonFhlProperty.income.flatMap(_.reversePremiums), "/ukNonFhlProperty/income/reversePremiums"),
      (ukNonFhlProperty.income.flatMap(_.periodAmount), "/ukNonFhlProperty/income/periodAmount"),
      (ukNonFhlProperty.income.flatMap(_.taxDeducted), "/ukNonFhlProperty/income/taxDeducted"),
      (ukNonFhlProperty.income.flatMap(_.otherIncome), "/ukNonFhlProperty/income/otherIncome"),
      (ukNonFhlProperty.income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukNonFhlProperty/income/rentARoom/rentsReceived"),
      (ukNonFhlProperty.expenses.flatMap(_.premisesRunningCosts), "/ukNonFhlProperty/expenses/premisesRunningCosts"),
      (ukNonFhlProperty.expenses.flatMap(_.repairsAndMaintenance), "/ukNonFhlProperty/expenses/repairsAndMaintenance"),
      (ukNonFhlProperty.expenses.flatMap(_.financialCosts), "/ukNonFhlProperty/expenses/financialCosts"),
      (ukNonFhlProperty.expenses.flatMap(_.professionalFees), "/ukNonFhlProperty/expenses/professionalFees"),
      (ukNonFhlProperty.expenses.flatMap(_.costOfServices), "/ukNonFhlProperty/expenses/costOfServices"),
      (ukNonFhlProperty.expenses.flatMap(_.other), "/ukNonFhlProperty/expenses/other"),
      (ukNonFhlProperty.expenses.flatMap(_.residentialFinancialCost), "/ukNonFhlProperty/expenses/residentialFinancialCost"),
      (ukNonFhlProperty.expenses.flatMap(_.consolidatedExpenses), "/ukNonFhlProperty/expenses/consolidatedExpenses"),
      (ukNonFhlProperty.expenses.flatMap(_.travelCosts), "/ukNonFhlProperty/expenses/travelCosts"),
      (
        ukNonFhlProperty.expenses.flatMap(_.residentialFinancialCostsCarriedForward),
        "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward"),
      (ukNonFhlProperty.expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukNonFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = ukNonFhlProperty.expenses
      .map(_.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          ukNonFhlProperty.expenses match {
            case Some(Def1_Amend_UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_))) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses")))
          }
      })
      .getOrElse(valid)

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)

  }
}
