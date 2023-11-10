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

package v4.controllers.validators

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveFromAndToDates, ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v4.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequestData
import v4.models.request.createForeignPropertyPeriodSummary.foreignFhlEea.{CreateForeignFhlEea, CreateForeignFhlEeaExpenses}
import v4.models.request.createForeignPropertyPeriodSummary.foreignPropertyEntry.{
  CreateForeignNonFhlPropertyEntry,
  CreateForeignNonFhlPropertyExpenses
}

object CreateForeignPropertyPeriodSummaryRulesValidator extends RulesValidator[CreateForeignPropertyPeriodSummaryRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(
      parsed: CreateForeignPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], CreateForeignPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    combine(
      ResolveFromAndToDates((fromDate, toDate)),
      foreignFhlEea.map(validateForeignFhlEea).getOrElse(valid),
      foreignNonFhlProperty.map(validateForeignNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateForeignFhlEea(foreignFhlEea: CreateForeignFhlEea): Validated[Seq[MtdError], Unit] = {
    import foreignFhlEea._

    val validatedConsolidatedExpenses = expenses match {
      case Some(CreateForeignFhlEeaExpenses(None, None, None, None, None, None, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses"))))
          .getOrElse(valid)
    }

    val valuesWithPaths = List(
      (income.flatMap(_.rentAmount), "/foreignFhlEea/income/rentAmount"),
      (expenses.flatMap(_.premisesRunningCosts), "/foreignFhlEea/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), "/foreignFhlEea/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), "/foreignFhlEea/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), "/foreignFhlEea/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), "/foreignFhlEea/expenses/costOfServices"),
      (expenses.flatMap(_.travelCosts), "/foreignFhlEea/expenses/travelCosts"),
      (expenses.flatMap(_.other), "/foreignFhlEea/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), "/foreignFhlEea/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlProperty(foreignNonFhlProperty: Seq[CreateForeignNonFhlPropertyEntry]): Validated[Seq[MtdError], Unit] = {
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

  private def validateForeignNonFhlPropertyEntry(entry: CreateForeignNonFhlPropertyEntry, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._

    val valuesWithPaths = List(
      (income.flatMap(_.rentIncome.flatMap(_.rentAmount)), s"/foreignNonFhlProperty/$index/income/rentIncome/rentAmount"),
      (income.flatMap(_.premiumsOfLeaseGrant), s"/foreignNonFhlProperty/$index/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.otherPropertyIncome), s"/foreignNonFhlProperty/$index/income/otherPropertyIncome"),
      (income.flatMap(_.foreignTaxPaidOrDeducted), s"/foreignNonFhlProperty/$index/income/foreignTaxPaidOrDeducted"),
      (income.flatMap(_.specialWithholdingTaxOrUkTaxPaid), s"/foreignNonFhlProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"),
      (expenses.flatMap(_.premisesRunningCosts), s"/foreignNonFhlProperty/$index/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), s"/foreignNonFhlProperty/$index/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), s"/foreignNonFhlProperty/$index/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), s"/foreignNonFhlProperty/$index/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), s"/foreignNonFhlProperty/$index/expenses/costOfServices"),
      (expenses.flatMap(_.travelCosts), s"/foreignNonFhlProperty/$index/expenses/travelCosts"),
      (expenses.flatMap(_.residentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.broughtFwdResidentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/broughtFwdResidentialFinancialCost"),
      (expenses.flatMap(_.other), s"/foreignNonFhlProperty/$index/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), s"/foreignNonFhlProperty/$index/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignNonFhlProperty/$index/countryCode")

    val validatedConsolidatedExpenses = expenses match {
      case Some(CreateForeignNonFhlPropertyExpenses(None, None, None, None, None, None, _, _, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath(s"/foreignNonFhlProperty/$index/expenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields :+ validatedCountryCode :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
