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

package v6.amendUkPropertyPeriodSummary.def1

import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers._
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits._
import common.models.errors.RuleBothExpensesSuppliedError
import v6.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty.{Def1_Amend_UkFhlProperty, Def1_Amend_UkFhlPropertyExpenses}
import v6.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty.{Def1_Amend_UkNonFhlProperty, Def1_Amend_UkNonFhlPropertyExpenses}
import v6.amendUkPropertyPeriodSummary.model.request.Def1_AmendUkPropertyPeriodSummaryRequestData

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
    import ukFhlProperty._
    val valuesWithPaths = List(
      (income.flatMap(_.periodAmount), "/ukFhlProperty/income/periodAmount"),
      (income.flatMap(_.taxDeducted), "/ukFhlProperty/income/taxDeducted"),
      (income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukFhlProperty/income/rentARoom/rentsReceived"),
      (expenses.flatMap(_.premisesRunningCosts), "/ukFhlProperty/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/ukFhlProperty/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/ukFhlProperty/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/ukFhlProperty/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/ukFhlProperty/expenses/costOfServices"),
      (expenses.flatMap(_.other), "/ukFhlProperty/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), "/ukFhlProperty/expenses/consolidatedExpenses"),
      (expenses.flatMap(_.travelCosts), "/ukFhlProperty/expenses/travelCosts"),
      (expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses
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
    import ukNonFhlProperty._
    val valuesWithPaths = List(
      (income.flatMap(_.premiumsOfLeaseGrant), "/ukNonFhlProperty/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.reversePremiums), "/ukNonFhlProperty/income/reversePremiums"),
      (income.flatMap(_.periodAmount), "/ukNonFhlProperty/income/periodAmount"),
      (income.flatMap(_.taxDeducted), "/ukNonFhlProperty/income/taxDeducted"),
      (income.flatMap(_.otherIncome), "/ukNonFhlProperty/income/otherIncome"),
      (income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukNonFhlProperty/income/rentARoom/rentsReceived"),
      (expenses.flatMap(_.premisesRunningCosts), "/ukNonFhlProperty/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/ukNonFhlProperty/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/ukNonFhlProperty/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/ukNonFhlProperty/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/ukNonFhlProperty/expenses/costOfServices"),
      (expenses.flatMap(_.other), "/ukNonFhlProperty/expenses/other"),
      (expenses.flatMap(_.residentialFinancialCost), "/ukNonFhlProperty/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.consolidatedExpenses), "/ukNonFhlProperty/expenses/consolidatedExpenses"),
      (expenses.flatMap(_.travelCosts), "/ukNonFhlProperty/expenses/travelCosts"),
      (expenses.flatMap(_.residentialFinancialCostsCarriedForward), "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward"),
      (expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukNonFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses
      .map(_.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          expenses match {
            case Some(Def1_Amend_UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_))) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses")))
          }
      })
      .getOrElse(valid)

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)

  }

}
