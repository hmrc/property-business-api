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

package v5.createAmendUkPropertyCumulativeSummary.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveFromAndToDates, ResolveParsedNumber}
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request.{Def1_CreateAmendUkPropertyCumulativeSummaryRequestData, Expenses, UkProperty}

class Def1_CreateUkPropertyCumulativeSummaryRulesValidator extends RulesValidator[Def1_CreateAmendUkPropertyCumulativeSummaryRequestData] {

  private val resolveParsedNumber              = ResolveParsedNumber()
  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(parsed: Def1_CreateAmendUkPropertyCumulativeSummaryRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendUkPropertyCumulativeSummaryRequestData] = {

    import parsed.body._

    combine(
      ResolveFromAndToDates((fromDate, toDate)),
      validateUkProperty(ukProperty)
    ).onSuccess(parsed)

  }

  private def validateUkProperty(ukProperty: UkProperty): Validated[Seq[MtdError], Unit] = {

    val maybeNegativeValues = List(
      (ukProperty.expenses.flatMap(_.premisesRunningCosts), "/ukProperty/expenses/premisesRunningCosts"),
      (ukProperty.expenses.flatMap(_.repairsAndMaintenance), "/ukProperty/expenses/repairsAndMaintenance"),
      (ukProperty.expenses.flatMap(_.financialCosts), "/ukProperty/expenses/financialCosts"),
      (ukProperty.expenses.flatMap(_.professionalFees), "/ukProperty/expenses/professionalFees"),
      (ukProperty.expenses.flatMap(_.costOfServices), "/ukProperty/expenses/costOfServices"),
      (ukProperty.expenses.flatMap(_.other), "/ukProperty/expenses/other"),
      (ukProperty.expenses.flatMap(_.travelCosts), "/ukProperty/expenses/travelCosts"),
      (ukProperty.expenses.flatMap(_.consolidatedExpenses), "/ukProperty/expenses/consolidatedExpenses")
    )

    val maybeValues = List(
      (ukProperty.income.flatMap(_.premiumsOfLeaseGrant), "/ukProperty/income/premiumsOfLeaseGrant"),
      (ukProperty.income.flatMap(_.reversePremiums), "/ukProperty/income/reversePremiums"),
      (ukProperty.income.flatMap(_.periodAmount), "/ukProperty/income/periodAmount"),
      (ukProperty.income.flatMap(_.taxDeducted), "/ukProperty/income/taxDeducted"),
      (ukProperty.income.flatMap(_.otherIncome), "/ukProperty/income/otherIncome"),
      (ukProperty.income.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukProperty/income/rentARoom/rentsReceived"),
      (ukProperty.expenses.flatMap(_.residentialFinancialCost), "/ukProperty/expenses/residentialFinancialCost"),
      (ukProperty.expenses.flatMap(_.residentialFinancialCostsCarriedForward), "/ukProperty/expenses/residentialFinancialCostsCarriedForward"),
      (ukProperty.expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNegativeNumberFields = maybeNegativeValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
    }

    val validatedNumberFields = maybeValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedConsolidateExpenses = ukProperty.expenses match {
      case Some(expenses) => validateConsolidatedExpenses(expenses)
      case _              => valid
    }

    combine(validatedNegativeNumberFields, validatedNumberFields, validatedConsolidateExpenses)

  }

  private def validateConsolidatedExpenses(expenses: Expenses): Validated[Seq[MtdError], Unit] = {
    expenses match {
      case Expenses(None, None, None, None, None, None, _, None, _, _, Some(_)) => valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukProperty/expenses"))))
          .getOrElse(valid)
    }
  }

}
