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

package v4.amendUkPropertyPeriodSummary.def2

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveParsedNumber
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits._
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty.{Def2_Amend_UkFhlProperty, Def2_Amend_UkFhlPropertyExpenses}
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty.{Def2_Amend_UkNonFhlProperty, Def2_Amend_UkNonFhlPropertyExpenses}
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Amend_UkPropertyExpensesRentARoom
import v4.amendUkPropertyPeriodSummary.model.request.Def2_AmendUkPropertyPeriodSummaryRequestData

class Def2_AmendUkPropertyPeriodSummaryRulesValidator extends RulesValidator[Def2_AmendUkPropertyPeriodSummaryRequestData] {

  private val resolveNonNegativeParsedNumber   = ResolveParsedNumber()
  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(
      parsed: Def2_AmendUkPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], Def2_AmendUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._
    combine(
      ukFhlProperty.map(validateUkFhlProperty).getOrElse(valid),
      ukNonFhlProperty.map(validateUkNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateUkFhlProperty(ukFhlProperty: Def2_Amend_UkFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukFhlProperty._
    val nonNegativeUkFhlProperty = List(
      (income.flatMap(_.periodAmount), "/ukFhlProperty/income/periodAmount"),
      (income.flatMap(_.taxDeducted), "/ukFhlProperty/income/taxDeducted"),
      (income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukFhlProperty/income/rentARoom/rentsReceived"),
      (expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val maybeNegativeUkFhlProperty = List(
      (expenses.flatMap(_.premisesRunningCosts), "/ukFhlProperty/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/ukFhlProperty/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/ukFhlProperty/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/ukFhlProperty/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/ukFhlProperty/expenses/costOfServices"),
      (expenses.flatMap(_.other), "/ukFhlProperty/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), "/ukFhlProperty/expenses/consolidatedExpenses"),
      (expenses.flatMap(_.travelCosts), "/ukFhlProperty/expenses/travelCosts")
    )

    val validatedNonNegativeNumberFields = nonNegativeUkFhlProperty
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveNonNegativeParsedNumber(number, path)
      }

    val validatedMaybeNegativeNumberFields = maybeNegativeUkFhlProperty
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses match {
      case Some(expenses) => validateFhlConsolidatedExpenses(expenses)
      case None           => valid
    }

    (validatedNonNegativeNumberFields ++ validatedMaybeNegativeNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

  private def validateFhlConsolidatedExpenses(expenses: Def2_Amend_UkFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
    expenses match {
      case Def2_Amend_UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None) => valid
      case Def2_Amend_UkFhlPropertyExpenses(
            None,
            None,
            None,
            None,
            None,
            None,
            Some(_),
            None,
            Some(Def2_Amend_UkPropertyExpensesRentARoom(Some(_)))) =>
        valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses"))))
          .getOrElse(valid)
    }
  }

  private def validateUkNonFhlProperty(ukNonFhlProperty: Def2_Amend_UkNonFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukNonFhlProperty._
    val nonNegativeUkNonFhlProperty = List(
      (income.flatMap(_.premiumsOfLeaseGrant), "/ukNonFhlProperty/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.reversePremiums), "/ukNonFhlProperty/income/reversePremiums"),
      (income.flatMap(_.periodAmount), "/ukNonFhlProperty/income/periodAmount"),
      (income.flatMap(_.taxDeducted), "/ukNonFhlProperty/income/taxDeducted"),
      (income.flatMap(_.otherIncome), "/ukNonFhlProperty/income/otherIncome"),
      (income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukNonFhlProperty/income/rentARoom/rentsReceived"),
      (expenses.flatMap(_.residentialFinancialCost), "/ukNonFhlProperty/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.residentialFinancialCostsCarriedForward), "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward"),
      (expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukNonFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val maybeNegativeUkNonFhlProperty = List(
      (expenses.flatMap(_.premisesRunningCosts), "/ukNonFhlProperty/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/ukNonFhlProperty/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/ukNonFhlProperty/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/ukNonFhlProperty/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/ukNonFhlProperty/expenses/costOfServices"),
      (expenses.flatMap(_.other), "/ukNonFhlProperty/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), "/ukNonFhlProperty/expenses/consolidatedExpenses"),
      (expenses.flatMap(_.travelCosts), "/ukNonFhlProperty/expenses/travelCosts")
    )

    val validatedNonNegativeNumberFields = nonNegativeUkNonFhlProperty
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveNonNegativeParsedNumber(number, path)
      }
    val validatedMaybeNegativeNumberFields = maybeNegativeUkNonFhlProperty
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses match {
      case Some(expenses) => validateNonFhlConsolidatedExpenses(expenses)
      case None           => valid
    }

    (validatedNonNegativeNumberFields ++ validatedMaybeNegativeNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)

  }

  private def validateNonFhlConsolidatedExpenses(expenses: Def2_Amend_UkNonFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
    expenses match {
      case Def2_Amend_UkNonFhlPropertyExpenses(None, None, None, None, None, None, _, None, _, _, Some(_)) => valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses"))))
          .getOrElse(valid)
    }
  }

}
