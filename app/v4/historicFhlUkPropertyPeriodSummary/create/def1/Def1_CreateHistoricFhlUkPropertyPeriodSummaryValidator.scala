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

package v4.historicFhlUkPropertyPeriodSummary.create.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v4.historicFhlUkPropertyPeriodSummary.create.def1.model.request.{UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v4.historicFhlUkPropertyPeriodSummary.create.model.request._


class Def1_CreateHistoricFhlUkPropertyPeriodSummaryValidator(nino: String, body: JsValue, appConfig: AppConfig)
  extends Validator[CreateHistoricFhlUkPropertyPeriodSummaryRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolveJson(body)
      ).mapN(Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData) andThen validateBusinessRules

  private def validateBusinessRules(
                                     parsed: Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData): Validated[Seq[MtdError], CreateHistoricFhlUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    val validatedDates = ResolveFromAndToDates((fromDate, toDate))

    val validatedIncome = income.map(validateIncome).getOrElse(valid)
    val validatedExpenses = expenses.map(validateExpenses).getOrElse(valid)

    List(validatedDates, validatedIncome, validatedExpenses)
      .traverse(identity)
      .map(_ => parsed)
  }

  private def validateIncome(income: UkFhlPropertyIncome): Validated[Seq[MtdError], Unit] = {
    import income._

    val valuesWithPaths = List(
      (periodAmount, "/income/periodAmount"),
      (taxDeducted, "/income/taxDeducted"),
      (rentARoom.flatMap(_.rentsReceived), "/income/rentARoom/rentsReceived")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _) => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    validatedNumberFields.sequence.andThen(_ => valid)
  }

  private def validateExpenses(expenses: UkFhlPropertyExpenses): Validated[Seq[MtdError], Unit] = {
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
      (rentARoom.flatMap(_.amountClaimed), "/expenses/rentARoom/amountClaimed")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _) => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedConsolidatedExpenses = expenses match {
      case UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None) => valid
      case _ =>
        expenses.consolidatedExpenses
          .map(_ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/expenses"))))
          .getOrElse(valid)
    }

    (validatedNumberFields :+ validatedConsolidatedExpenses).sequence.andThen(_ => valid)
  }


}
