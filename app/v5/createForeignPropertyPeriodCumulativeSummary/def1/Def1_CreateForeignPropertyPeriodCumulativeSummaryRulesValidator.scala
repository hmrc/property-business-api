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

package v5.createForeignPropertyPeriodCumulativeSummary.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveFromAndToDates, ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.{
  Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData,
  Expenses,
  ForeignProperty
}

object Def1_CreateForeignPropertyPeriodCumulativeSummaryRulesValidator
    extends RulesValidator[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData)
      : Validated[Seq[MtdError], Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData] = {
    import parsed.body._

    combine(
      ResolveFromAndToDates((fromDate, toDate)),
      foreignProperty.map(validateForeignProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateForeignProperty(foreignProperty: Seq[ForeignProperty]): Validated[Seq[MtdError], Unit] = {
    val zippedForeignProperties = foreignProperty.zipWithIndex

    val validatedCountryCodes = zippedForeignProperties
      .map { case (entry, index) =>
        (entry.countryCode, s"/foreignProperty/$index/countryCode")
      }
      .groupBy(_._1)
      .collect {
        case (code, codeAndPaths) if codeAndPaths.size >= 2 =>
          Invalid(List(RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(code, codeAndPaths.map(_._2))))
      }
      .toSeq

    val validatedEntries = zippedForeignProperties
      .map { case (entry, index) => validateForeignPropertyEntry(entry, index) }
      .traverse(identity)

    (validatedCountryCodes :+ validatedEntries).sequence.andThen(_ => valid)
  }

  private def validateForeignPropertyEntry(entry: ForeignProperty, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._
    val valuesWithPaths = List(
      (income.flatMap(_.rentIncome.flatMap(_.rentAmount)), s"/foreignProperty/$index/income/rentIncome/rentAmount"),
      (income.flatMap(_.premiumsOfLeaseGrant), s"/foreignProperty/$index/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.otherPropertyIncome), s"/foreignProperty/$index/income/otherPropertyIncome"),
      (income.flatMap(_.foreignTaxPaidOrDeducted), s"/foreignProperty/$index/income/foreignTaxPaidOrDeducted"),
      (income.flatMap(_.specialWithholdingTaxOrUkTaxPaid), s"/foreignProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"),
      (expenses.flatMap(_.premisesRunningCosts), s"/foreignProperty/$index/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), s"/foreignProperty/$index/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), s"/foreignProperty/$index/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), s"/foreignProperty/$index/expenses/professionalFees"),
      (expenses.flatMap(_.costOfServices), s"/foreignProperty/$index/expenses/costOfServices"),
      (expenses.flatMap(_.travelCosts), s"/foreignProperty/$index/expenses/travelCosts"),
      (expenses.flatMap(_.residentialFinancialCost), s"/foreignProperty/$index/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.broughtFwdResidentialFinancialCost), s"/foreignProperty/$index/expenses/broughtFwdResidentialFinancialCost"),
      (expenses.flatMap(_.other), s"/foreignProperty/$index/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), s"/foreignProperty/$index/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignProperty/$index/countryCode")

    val validatedConsolidatedExpenses = expenses match {
      case Some(Expenses(None, None, None, None, None, None, _, _, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath(s"/foreignProperty/$index/expenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields :+ validatedCountryCode :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
