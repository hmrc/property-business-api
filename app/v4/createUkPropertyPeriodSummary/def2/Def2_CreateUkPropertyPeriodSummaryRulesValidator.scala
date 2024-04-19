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

package v4.createUkPropertyPeriodSummary.def2

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveFromAndToDates, ResolveParsedNumber}
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty._
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty._
import v4.createUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Create_UkPropertyExpensesRentARoom
import v4.createUkPropertyPeriodSummary.model.request.Def2_CreateUkPropertyPeriodSummaryRequestData

class Def2_CreateUkPropertyPeriodSummaryRulesValidator extends RulesValidator[Def2_CreateUkPropertyPeriodSummaryRequestData] {

  private val resolveParsedNumber              = ResolveParsedNumber()
  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(
      parsed: Def2_CreateUkPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], Def2_CreateUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    combine(
      ResolveFromAndToDates((fromDate, toDate)),
      ukFhlProperty.map(validateUkFhlProperty).getOrElse(valid),
      ukNonFhlProperty.map(validateUkNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateUkFhlProperty(ukFhlProperty: Def2_Create_UkFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukFhlProperty.{expenses => maybeExpenses, income => maybeIncome}

    val maybeNegativeValues = List(
      (maybeExpenses.flatMap(_.premisesRunningCosts), "/ukFhlProperty/expenses/premisesRunningCosts"),
      (maybeExpenses.flatMap(_.repairsAndMaintenance), "/ukFhlProperty/expenses/repairsAndMaintenance"),
      (maybeExpenses.flatMap(_.financialCosts), "/ukFhlProperty/expenses/financialCosts"),
      (maybeExpenses.flatMap(_.professionalFees), "/ukFhlProperty/expenses/professionalFees"),
      (maybeExpenses.flatMap(_.costOfServices), "/ukFhlProperty/expenses/costOfServices"),
      (maybeExpenses.flatMap(_.other), "/ukFhlProperty/expenses/other"),
      (maybeExpenses.flatMap(_.consolidatedExpenses), "/ukFhlProperty/expenses/consolidatedExpenses"),
      (maybeExpenses.flatMap(_.travelCosts), "/ukFhlProperty/expenses/travelCosts")
    )

    val maybeValues = List(
      (maybeIncome.flatMap(_.periodAmount), "/ukFhlProperty/income/periodAmount"),
      (maybeIncome.flatMap(_.taxDeducted), "/ukFhlProperty/income/taxDeducted"),
      (maybeIncome.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukFhlProperty/income/rentARoom/rentsReceived"),
      (maybeExpenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNegativeNumberFields = maybeNegativeValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
    }

    val validatedNumberFields = maybeValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedConsolidatedExpenses = maybeExpenses match {
      case Some(expenses) => validateFhlConsolidatedExpenses(expenses)
      case None           => valid
    }

    combine(validatedNegativeNumberFields, validatedNumberFields, validatedConsolidatedExpenses)

  }

  private def validateFhlConsolidatedExpenses(expenses: Def2_Create_UkFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
    expenses match {
      case Def2_Create_UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None) => valid
      case Def2_Create_UkFhlPropertyExpenses(
            None,
            None,
            None,
            None,
            None,
            None,
            Some(_),
            None,
            Some(Def2_Create_UkPropertyExpensesRentARoom(Some(_)))) =>
        valid

      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukFhlProperty/expenses"))))
          .getOrElse(valid)
    }
  }

  private def validateUkNonFhlProperty(ukNonFhlProperty: Def2_Create_UkNonFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukNonFhlProperty.{expenses => maybeExpenses, income => maybeIncome}

    val maybeNegativeExpensesValues = List(
      (maybeExpenses.flatMap(_.premisesRunningCosts), "/ukNonFhlProperty/expenses/premisesRunningCosts"),
      (maybeExpenses.flatMap(_.repairsAndMaintenance), "/ukNonFhlProperty/expenses/repairsAndMaintenance"),
      (maybeExpenses.flatMap(_.financialCosts), "/ukNonFhlProperty/expenses/financialCosts"),
      (maybeExpenses.flatMap(_.professionalFees), "/ukNonFhlProperty/expenses/professionalFees"),
      (maybeExpenses.flatMap(_.costOfServices), "/ukNonFhlProperty/expenses/costOfServices"),
      (maybeExpenses.flatMap(_.other), "/ukNonFhlProperty/expenses/other"),
      (maybeExpenses.flatMap(_.travelCosts), "/ukNonFhlProperty/expenses/travelCosts"),
      (maybeExpenses.flatMap(_.consolidatedExpenses), "/ukNonFhlProperty/expenses/consolidatedExpenses")
    )

    val maybeValues = List(
      (maybeIncome.flatMap(_.premiumsOfLeaseGrant), "/ukNonFhlProperty/income/premiumsOfLeaseGrant"),
      (maybeIncome.flatMap(_.reversePremiums), "/ukNonFhlProperty/income/reversePremiums"),
      (maybeIncome.flatMap(_.periodAmount), "/ukNonFhlProperty/income/periodAmount"),
      (maybeIncome.flatMap(_.taxDeducted), "/ukNonFhlProperty/income/taxDeducted"),
      (maybeIncome.flatMap(_.otherIncome), "/ukNonFhlProperty/income/otherIncome"),
      (maybeIncome.flatMap(_.rentARoom.flatMap(_.rentsReceived)), "/ukNonFhlProperty/income/rentARoom/rentsReceived"),
      (maybeExpenses.flatMap(_.residentialFinancialCost), "/ukNonFhlProperty/expenses/residentialFinancialCost"),
      (maybeExpenses.flatMap(_.residentialFinancialCostsCarriedForward), "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward"),
      (maybeExpenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)), "/ukNonFhlProperty/expenses/rentARoom/amountClaimed")
    )

    val validatedNonNegativeNumberFields = maybeValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedMaybeNegativeNumberFields = maybeNegativeExpensesValues.traverse {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
    }

    val validatedConsolidatedExpenses = maybeExpenses match {
      case Some(expenses) => validateNonFhlConsolidatedExpenses(expenses)
      case None           => valid
    }

    combine(validatedNonNegativeNumberFields, validatedMaybeNegativeNumberFields, validatedConsolidatedExpenses)

  }

  private def validateNonFhlConsolidatedExpenses(expenses: Def2_Create_UkNonFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
    expenses match {
      case Def2_Create_UkNonFhlPropertyExpenses(None, None, None, None, None, None, _, None, _, _, Some(_)) => valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/ukNonFhlProperty/expenses"))))
          .getOrElse(valid)
    }
  }

}
