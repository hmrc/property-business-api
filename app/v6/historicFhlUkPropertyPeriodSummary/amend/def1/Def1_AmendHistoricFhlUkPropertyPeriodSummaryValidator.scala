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

package v6.historicFhlUkPropertyPeriodSummary.amend.def1

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.*
import common.controllers.validators.resolvers.ResolvePeriodId
import common.models.errors.RuleBothExpensesSuppliedError
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.historicFhlUkPropertyPeriodSummary.amend.def1.model.request.UkFhlPropertyExpenses
import v6.historicFhlUkPropertyPeriodSummary.amend.request.*

import javax.inject.Inject

class Def1_AmendHistoricFhlUkPropertyPeriodSummaryValidator @Inject() (nino: String, periodId: String, body: JsValue)(implicit
    config: PropertyBusinessConfig)
    extends Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] {

  private val resolvePeriodId = new ResolvePeriodId(TaxYear.fromMtd(config.historicMinimumTaxYear), TaxYear.fromMtd(config.historicMaximumTaxYear))

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolvePeriodId(periodId),
      resolveJson(body)
    ).mapN(Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData) andThen validateBusinessRules

  private def validateBusinessRules(parsed: Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData)
      : Validated[Seq[MtdError], AmendHistoricFhlUkPropertyPeriodSummaryRequestData] = {
    import parsed.body._

    val validatedIncome = income
      .map { i =>
        import i._

        List(
          (periodAmount, "/income/periodAmount"),
          (taxDeducted, "/income/taxDeducted"),
          (rentARoom.flatMap(_.rentsReceived), "/income/rentARoom/rentsReceived")
        ).map {
          case (None, _)            => valid
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
          case (None, _)            => valid
          case (Some(number), path) => resolveParsedNumber(number, path)
        }
      }
      .getOrElse(Nil)

    val validatedBothExpenses = expenses
      .map(_.consolidatedExpenses match {
        case None => valid
        case Some(_) =>
          expenses match {
            case Some(UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None)) => valid
            case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses")))
          }
      })
      .getOrElse(valid)

    (validatedIncome ++ validatedExpenses :+ validatedBothExpenses).sequence.andThen(_ => Valid(parsed))

  }

}
