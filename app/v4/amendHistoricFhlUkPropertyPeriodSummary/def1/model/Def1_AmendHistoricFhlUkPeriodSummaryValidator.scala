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

package v4.amendHistoricFhlUkPropertyPeriodSummary.def1.model

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue

import v4.amendHistoricFhlUkPropertyPeriodSummary.def1.model.request.UkFhlPieExpenses
import v4.amendHistoricFhlUkPropertyPeriodSummary.model.request._


class Def1_AmendHistoricFhlUkPeriodSummaryValidator (nino: String, periodId: String, body: JsValue, appConfig: AppConfig)
  extends Validator[AmendHistoricFhlUkPiePeriodSummaryRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxYear = appConfig.maximumTaxYearHistoric

  private lazy val resolvePeriodId = new ResolvePeriodId(minimumTaxYear, maximumTaxYear)

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], AmendHistoricFhlUkPiePeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolvePeriodId(periodId),
      resolveJson(body)
      ).mapN(Def1_AmendHistoricFhlUkPiePeriodSummaryRequestData) andThen validateBusinessRules

  private def validateBusinessRules(
                                     parsed: Def1_AmendHistoricFhlUkPiePeriodSummaryRequestData): Validated[Seq[MtdError], AmendHistoricFhlUkPiePeriodSummaryRequestData] = {
    import parsed.body._

    val validatedIncome = income
      .map { i =>
        import i._

        List(
          (periodAmount, "/income/periodAmount"),
          (taxDeducted, "/income/taxDeducted"),
          (rentARoom.flatMap(_.rentsReceived), "/income/rentARoom/rentsReceived")
        ).map {
          case (None, _) => valid
          case (Some(number), path) => resolveParsedNumber(number, path)
        }
      }
      .getOrElse(Nil)

    val validatedExpenses = expenses
      .map { e =>
        import e._

        List(
          (premisesRunningCosts, "/expenses/premisesRunningCosts"),
          (repairsAndMaintenance, "/expenses/repairsAndMaintenance"),
          (financialCosts, "/expenses/premisesCosts"),
          (professionalFees, "/expenses/professionalFees"),
          (costOfServices, "/expenses/costOfServices"),
          (other, "/expenses/other"),
          (consolidatedExpenses, "/expenses/consolidatedExpenses"),
          (travelCosts, "/expenses/travelCosts"),
          (rentARoom.flatMap(_.amountClaimed), "/income/rentARoom/amountClaimed")
        ).map {
          case (None, _) => valid
          case (Some(number), path) => resolveParsedNumber(number, path)
        }
      }
      .getOrElse(Nil)

    val validatedBothExpenses = expenses
      .map(_.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          expenses match {
            case Some(UkFhlPieExpenses(None, None, None, None, None, None, Some(_), None, None)) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses")))
          }
      })
      .getOrElse(valid)

    (validatedIncome ++ validatedExpenses :+ validatedBothExpenses).sequence.andThen(_ => Valid(parsed))

  }


}
