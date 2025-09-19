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

package v6.amendForeignPropertyPeriodSummary.def1

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import common.models.errors.{RuleBothExpensesSuppliedError, RuleDuplicateCountryCodeError}
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.MtdError
import v6.amendForeignPropertyPeriodSummary.def1.model.request.foreignFhlEea.{AmendForeignFhlEea, AmendForeignFhlEeaExpenses}
import v6.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry.{
  AmendForeignNonFhlPropertyEntry,
  AmendForeignNonFhlPropertyExpenses
}
import v6.amendForeignPropertyPeriodSummary.model.request.Def1_AmendForeignPropertyPeriodSummaryRequestData

object Def1_AmendForeignPropertyPeriodSummaryRulesValidator extends RulesValidator[Def1_AmendForeignPropertyPeriodSummaryRequestData] {
  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(
      parsed: Def1_AmendForeignPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], Def1_AmendForeignPropertyPeriodSummaryRequestData] = {
    import parsed.body.*

    List(
      foreignFhlEea.map(validateForeignFhlEea).getOrElse(valid),
      foreignNonFhlProperty.map(validateForeignNonFhlProperties).getOrElse(valid)
    ).traverse(identity).map(_ => parsed)
  }

  private def validateForeignFhlEea(foreignFhlEea: AmendForeignFhlEea): Validated[Seq[MtdError], Unit] = {
    import foreignFhlEea.*

    val fieldsWithPaths = List(
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

    val validateNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedForeignFhlEeaConsolidatedExpenses = expenses.map { expenses =>
      expenses.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          expenses match {
            case AmendForeignFhlEeaExpenses(None, None, None, None, None, None, None, Some(_)) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/foreignFhlEea/expenses")))
          }
      }
    }

    (validateNumberFields ++ validatedForeignFhlEeaConsolidatedExpenses).sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlProperties(foreignNonFhlProperties: Seq[AmendForeignNonFhlPropertyEntry]): Validated[Seq[MtdError], Unit] = {
    val zippedForeignNonFhlProperties = foreignNonFhlProperties.zipWithIndex

    val validatedFields = zippedForeignNonFhlProperties
      .map { case (entry, index) =>
        List(validateForeignPropertyConsolidatedExpenses(entry, index), validateForeignNonFhlProperty(entry, index)).sequence
      }
      .sequence
      .andThen(_ => valid)

    val validatedCountryCodes: Seq[Validated[Seq[MtdError], Unit]] = zippedForeignNonFhlProperties
      .map { case (entry, index) =>
        (entry.countryCode, s"/foreignNonFhlProperty/$index/countryCode")
      }
      .groupBy(_._1)
      .collect {
        case (code, codeAndPaths) if codeAndPaths.size >= 2 =>
          Invalid(List(RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(code, codeAndPaths.map(_._2))))
      }
      .toSeq

    (validatedCountryCodes :+ validatedFields).sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlProperty(foreignNonFhlPropertyEntry: AmendForeignNonFhlPropertyEntry,
                                            index: Int): Validated[Seq[MtdError], Unit] = {
    import foreignNonFhlPropertyEntry.{countryCode, expenses as e, income as i}

    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignNonFhlProperty/$index/countryCode")

    val fieldsWithPaths = List(
      (i.flatMap(_.rentIncome).flatMap(_.rentAmount), s"/foreignNonFhlProperty/$index/income/rentIncome/rentAmount"),
      (i.flatMap(_.premiumsOfLeaseGrant), s"/foreignNonFhlProperty/$index/income/premiumsOfLeaseGrant"),
      (i.flatMap(_.otherPropertyIncome), s"/foreignNonFhlProperty/$index/income/otherPropertyIncome"),
      (i.flatMap(_.foreignTaxPaidOrDeducted), s"/foreignNonFhlProperty/$index/income/foreignTaxPaidOrDeducted"),
      (i.flatMap(_.specialWithholdingTaxOrUkTaxPaid), s"/foreignNonFhlProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"),
      (e.flatMap(_.premisesRunningCosts), s"/foreignNonFhlProperty/$index/expenses/premisesRunningCosts"),
      (e.flatMap(_.repairsAndMaintenance), s"/foreignNonFhlProperty/$index/expenses/repairsAndMaintenance"),
      (e.flatMap(_.financialCosts), s"/foreignNonFhlProperty/$index/expenses/financialCosts"),
      (e.flatMap(_.professionalFees), s"/foreignNonFhlProperty/$index/expenses/professionalFees"),
      (e.flatMap(_.costOfServices), s"/foreignNonFhlProperty/$index/expenses/costOfServices"),
      (e.flatMap(_.travelCosts), s"/foreignNonFhlProperty/$index/expenses/travelCosts"),
      (e.flatMap(_.residentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/residentialFinancialCost"),
      (e.flatMap(_.broughtFwdResidentialFinancialCost), s"/foreignNonFhlProperty/$index/expenses/broughtFwdResidentialFinancialCost"),
      (e.flatMap(_.other), s"/foreignNonFhlProperty/$index/expenses/other"),
      (e.flatMap(_.consolidatedExpenses), s"/foreignNonFhlProperty/$index/expenses/consolidatedExpenses")
    )

    val validatedNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val result = validatedNumberFields :+ validatedCountryCode

    result.sequence.andThen(_ => valid)
  }

  private def validateForeignPropertyConsolidatedExpenses(foreignNonFhlPropertyEntry: AmendForeignNonFhlPropertyEntry,
                                                          index: Int): Validated[Seq[MtdError], Unit] = {

    foreignNonFhlPropertyEntry.expenses
      .map { expenses =>
        expenses.consolidatedExpenses match {
          case None => valid
          case Some(_) =>
            expenses match {
              case AmendForeignNonFhlPropertyExpenses(None, None, None, None, None, None, _, _, None, Some(_)) => valid
              case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath(s"/foreignNonFhlProperty/$index/expenses")))
            }
        }
      }
      .getOrElse(valid)
  }

}
