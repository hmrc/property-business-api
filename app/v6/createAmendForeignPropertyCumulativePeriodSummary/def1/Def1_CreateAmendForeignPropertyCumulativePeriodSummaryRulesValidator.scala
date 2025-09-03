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

package v6.createAmendForeignPropertyCumulativePeriodSummary.def1

import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.*
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import common.models.errors.RuleBothExpensesSuppliedError
import common.utils.DateValidator
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.{
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData,
  Expenses,
  ForeignProperty
}

object Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRulesValidator
    extends RulesValidator[Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(parsed: Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] = {
    import parsed.body._

    combine(
      DateValidator.validateFromAndToDates(fromDate, toDate),
      validateForeignProperty(foreignProperty)
    ).onSuccess(parsed)
  }

  private def validateForeignProperty(foreignProperty: Seq[ForeignProperty]): Validated[Seq[MtdError], Unit] = {
    val zippedForeignProperties = foreignProperty.zipWithIndex

    val validatedEntries = zippedForeignProperties
      .map { case (entry, index) => validateForeignPropertyEntry(entry, index) }
      .traverse(identity)

    validatedEntries andThen (_ => valid)
  }

  private def validateForeignPropertyEntry(entry: ForeignProperty, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._
    val valuesWithPaths = List(
      (income.flatMap(_.rentIncome.flatMap(_.rentAmount)), s"/foreignProperty/$index/income/rentIncome/rentAmount"),
      (income.flatMap(_.premiumsOfLeaseGrant), s"/foreignProperty/$index/income/premiumsOfLeaseGrant"),
      (income.flatMap(_.otherPropertyIncome), s"/foreignProperty/$index/income/otherPropertyIncome"),
      (income.flatMap(_.foreignTaxPaidOrDeducted), s"/foreignProperty/$index/income/foreignTaxPaidOrDeducted"),
      (income.flatMap(_.specialWithholdingTaxOrUkTaxPaid), s"/foreignProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"),
      (expenses.flatMap(_.residentialFinancialCost), s"/foreignProperty/$index/expenses/residentialFinancialCost"),
      (expenses.flatMap(_.broughtFwdResidentialFinancialCost), s"/foreignProperty/$index/expenses/broughtFwdResidentialFinancialCost")
    )

    val maybeNegativeValuesWithPaths = List(
      (expenses.flatMap(_.premisesRunningCosts), s"/foreignProperty/$index/expenses/premisesRunningCosts"),
      (expenses.flatMap(_.repairsAndMaintenance), s"/foreignProperty/$index/expenses/repairsAndMaintenance"),
      (expenses.flatMap(_.financialCosts), s"/foreignProperty/$index/expenses/financialCosts"),
      (expenses.flatMap(_.professionalFees), s"/foreignProperty/$index/expenses/professionalFees"),
      (expenses.flatMap(_.travelCosts), s"/foreignProperty/$index/expenses/travelCosts"),
      (expenses.flatMap(_.costOfServices), s"/foreignProperty/$index/expenses/costOfServices"),
      (expenses.flatMap(_.other), s"/foreignProperty/$index/expenses/other"),
      (expenses.flatMap(_.consolidatedExpenses), s"/foreignProperty/$index/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }
    val validatedNegativeNumberFields = maybeNegativeValuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveMaybeNegativeParsedNumber(number, path)
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

    (validatedNumberFields ++ validatedNegativeNumberFields :+ validatedCountryCode :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
