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

package v4.createForeignPropertyPeriodSummary.def2

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveFromAndToDates, ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v4.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignFhlEea.{
  Def2_Create_CreateForeignFhlEea,
  Def2_Create_CreateForeignFhlEeaExpenses
}
import v4.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignPropertyEntry.{
  Def2_Create_CreateForeignNonFhlPropertyEntry,
  Def2_Create_CreateForeignNonFhlPropertyExpenses
}
import v4.createForeignPropertyPeriodSummary.model.request.Def2_CreateForeignPropertyPeriodSummaryRequestData

object Def2_CreateForeignPropertyPeriodSummaryRulesValidator extends RulesValidator[Def2_CreateForeignPropertyPeriodSummaryRequestData] {

  private val resolveParsedNumber              = ResolveParsedNumber()
  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(
      parsed: Def2_CreateForeignPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], Def2_CreateForeignPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    combine(
      ResolveFromAndToDates((fromDate, toDate)),
      foreignFhlEea.map(validateForeignFhlEea).getOrElse(valid),
      foreignNonFhlProperty.map(validateForeignNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateForeignFhlEea(foreignFhlEea: Def2_Create_CreateForeignFhlEea): Validated[Seq[MtdError], Unit] = {
    import foreignFhlEea._

    val validatedConsolidatedExpenses = expenses match {
      case Some(Def2_Create_CreateForeignFhlEeaExpenses(None, None, None, None, None, None, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses"))))
          .getOrElse(valid)
    }

    val maybeNegativeValues = List(
      (expenses.flatMap(_.premisesRunningCosts), "/foreignFhlEea/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/foreignFhlEea/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/foreignFhlEea/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/foreignFhlEea/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/foreignFhlEea/expenses/costOfServices"),
      (expenses.flatMap(_.travelCosts), "/foreignFhlEea/expenses/travelCosts"),
      (expenses.flatMap(_.other), "/foreignFhlEea/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), "/foreignFhlEea/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = List((income.flatMap(_.rentAmount), "/foreignFhlEea/income/rentAmount")).map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedNegativeNumberFields = maybeNegativeValues.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
    }

    (validatedNumberFields ++ validatedNegativeNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlProperty(
      foreignNonFhlProperty: Seq[Def2_Create_CreateForeignNonFhlPropertyEntry]): Validated[Seq[MtdError], Unit] = {
    val zippedForeignNonFhlProperties = foreignNonFhlProperty.zipWithIndex

    val validatedCountryCodes = zippedForeignNonFhlProperties
      .map { case (entry, index) =>
        (entry.countryCode, s"/foreignNonFhlProperty/$index/countryCode")
      }
      .groupBy(_._1)
      .collect {
        case (code, codeAndPaths) if codeAndPaths.size >= 2 =>
          Invalid(List(RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(code, codeAndPaths.map(_._2))))
      }
      .toSeq

    val validatedEntries = zippedForeignNonFhlProperties
      .map { case (entry, index) => validateForeignNonFhlPropertyEntry(entry, index) }
      .traverse(identity)

    (validatedCountryCodes :+ validatedEntries).sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlPropertyEntry(entry: Def2_Create_CreateForeignNonFhlPropertyEntry, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._
    val maybeValues = List(
      (income.flatMap(_.rentIncome.flatMap(_.rentAmount)), s"/foreignNonFhlProperty/$index/income/rentIncome/rentAmount"),
      (income.flatMap(_.premiumsOfLeaseGrant), s"/foreignNonFhlProperty/$index/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.otherPropertyIncome), s"/foreignNonFhlProperty/$index/income/otherPropertyIncome"),
      (income.flatMap(_.foreignTaxPaidOrDeducted), s"/foreignNonFhlProperty/$index/income/foreignTaxPaidOrDeducted"),
      (income.flatMap(_.specialWithholdingTaxOrUkTaxPaid), s"/foreignNonFhlProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"),
      (expenses.flatMap(_.residentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.broughtFwdResidentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/broughtFwdResidentialFinancialCost")
    )
    val maybeNegativeValues = List(
      (expenses.flatMap(_.premisesRunningCosts), s"/foreignNonFhlProperty/$index/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), s"/foreignNonFhlProperty/$index/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), s"/foreignNonFhlProperty/$index/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), s"/foreignNonFhlProperty/$index/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), s"/foreignNonFhlProperty/$index/expenses/costOfServices"),
      (expenses.flatMap(_.travelCosts), s"/foreignNonFhlProperty/$index/expenses/travelCosts"),
      (expenses.flatMap(_.other), s"/foreignNonFhlProperty/$index/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), s"/foreignNonFhlProperty/$index/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = maybeValues.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }
    val validatedNegativeNumberFields = maybeNegativeValues.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
    }

    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignNonFhlProperty/$index/countryCode")

    val validatedConsolidatedExpenses = expenses match {
      case Some(Def2_Create_CreateForeignNonFhlPropertyExpenses(None, None, None, None, None, None, _, _, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath(s"/foreignNonFhlProperty/$index/expenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields ++ validatedNegativeNumberFields :+ validatedCountryCode :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
