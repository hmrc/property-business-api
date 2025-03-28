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

package v5.historicNonFhlUkPropertyPeriodSummary.create.def1

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import common.controllers.validators.resolvers.ResolveFromAndToDates
import common.models.errors.RuleBothExpensesSuppliedError
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber}
import shared.models.errors.MtdError
import v5.historicNonFhlUkPropertyPeriodSummary.create.def1.model.request.{UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v5.historicNonFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData
}

class Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryValidator(nino: String, body: JsValue)
    extends Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolveJson(body)
    ).mapN(Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData) andThen validateBusinessRules

  private def validateBusinessRules(parsed: Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData)
      : Validated[Seq[MtdError], Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    val validatedDates = ResolveFromAndToDates((fromDate, toDate))

    val validatedIncome   = income.map(validateIncome).getOrElse(valid)
    val validatedExpenses = expenses.map(validateExpenses).getOrElse(valid)

    List(
      validatedDates,
      validatedIncome,
      validatedExpenses
    )
      .traverse(identity)
      .map(_ => parsed)
  }

  private def validateIncome(income: UkNonFhlPropertyIncome): Validated[Seq[MtdError], Unit] = {
    import income._

    val valuesWithPaths = List(
      (periodAmount, "/income/periodAmount"),
      (premiumsOfLeaseGrant, "/income/premiumsOfLeaseGrant"),
      (reversePremiums, "/income/reversePremiums"),
      (otherIncome, "/income/otherIncome"),
      (taxDeducted, "/income/taxDeducted"),
      (rentARoom.flatMap(_.rentsReceived), "/income/rentARoom/rentsReceived")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    validatedNumberFields.sequence.andThen(_ => valid)
  }

  private def validateExpenses(expenses: UkNonFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
    import expenses._

    val valuesWithPaths = List(
      (premisesRunningCosts, "/expenses/premisesRunningCosts"),
      (repairsAndMaintenance, "/expenses/repairsAndMaintenance"),
      (financialCosts, "/expenses/financialCosts"),
      (professionalFees, "/expenses/professionalFees"),
      (costOfServices, "/expenses/costOfServices"),
      (other, "/expenses/other"),
      (consolidatedExpenses, "/expenses/consolidatedExpenses"),
      (travelCosts, "/expenses/travelCosts"),
      (residentialFinancialCostsCarriedForward, "/expenses/residentialFinancialCostsCarriedForward"),
      (residentialFinancialCost, "/expenses/residentialFinancialCost"),
      (rentARoom.flatMap(_.amountClaimed), "/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses match {
      case UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_)) => valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }

}
