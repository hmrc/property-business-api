/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createAmendForeignPropertyCumulativePeriodSummary.def2

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import common.controllers.validators.resolvers.ResolveUuid
import common.models.domain.PropertyId
import common.models.errors.*
import common.utils.DateValidator
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.ResolveParsedNumber
import shared.models.errors.*
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.model.request
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.model.request.*

object Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRulesValidator
    extends RulesValidator[Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val resolveMaybeNegativeParsedNumber = ResolveParsedNumber(min = -99999999999.99)

  def validateBusinessRules(parsed: Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData)
      : Validated[Seq[MtdError], Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] = {
    import parsed.body.*

    combine(
      DateValidator.validateFromAndToDates(fromDate, toDate),
      validateForeignProperty(foreignProperty)
    ).onSuccess(parsed)
  }

  private def validateForeignProperty(foreignProperty: Seq[request.ForeignProperty]): Validated[Seq[MtdError], Unit] = {
    val zippedForeignProperties: Seq[(ForeignProperty, Int)] = foreignProperty.zipWithIndex

    val validatedEntries: Validated[Seq[MtdError], Unit] = zippedForeignProperties
      .map { case (entry, index) => validateForeignPropertyEntry(entry, index) }
      .sequence
      .andThen(_ => valid)

    val validatedPropertyIds: Seq[Validated[Seq[MtdError], Unit]] = zippedForeignProperties
      .groupMap(_._1.propertyId) { case (_, index) =>
        s"/foreignProperty/$index/propertyId"
      }
      .collect {
        case (id, paths) if paths.size > 1 => Invalid(List(RuleDuplicatePropertyIdError.forDuplicatedIdsAndPaths(id, paths)))
      }
      .toSeq

    (validatedPropertyIds :+ validatedEntries).sequence.andThen(_ => valid)
  }

  private def validateForeignPropertyEntry(entry: request.ForeignProperty, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry.*
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

    val validatedPropertyId = ResolveUuid(propertyId, PropertyIdFormatError.withPath(s"/foreignProperty/$index/propertyId")).apply(PropertyId.apply)

    val validatedConsolidatedExpenses = expenses match {
      case Some(request.Expenses(None, None, None, None, None, None, _, _, None, Some(_))) => valid
      case _ =>
        expenses
          .flatMap(_.consolidatedExpenses)
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath(s"/foreignProperty/$index/expenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields ++ validatedNegativeNumberFields :+ validatedPropertyId :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
