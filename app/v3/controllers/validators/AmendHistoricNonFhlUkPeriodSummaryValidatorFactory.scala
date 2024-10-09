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

package v3.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v3.models.request.amendHistoricNonFhlUkPiePeriodSummary.{
  AmendHistoricNonFhlUkPeriodSummaryRequestBody,
  AmendHistoricNonFhlUkPeriodSummaryRequestData,
  UkNonFhlPieExpenses,
  UkNonFhlPieIncome
}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendHistoricNonFhlUkPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  private val resolveParsedNumber  = ResolveParsedNumber()
  private val resolveJson          = new ResolveNonEmptyJsonObject[AmendHistoricNonFhlUkPeriodSummaryRequestBody]()
  private lazy val resolvePeriodId = new ResolvePeriodId(appConfig.minimumTaxYearHistoric, appConfig.maximumTaxYearHistoric)

  private val valid = Valid(())

  def validator(nino: String, periodId: String, body: JsValue): Validator[AmendHistoricNonFhlUkPeriodSummaryRequestData] =
    new Validator[AmendHistoricNonFhlUkPeriodSummaryRequestData] {

      def validate: Validated[Seq[MtdError], AmendHistoricNonFhlUkPeriodSummaryRequestData] =
        (
          ResolveNino(nino),
          resolvePeriodId(periodId),
          resolveJson(body)
        ).mapN(AmendHistoricNonFhlUkPeriodSummaryRequestData) andThen validateBusinessRules

      private def validateBusinessRules(
          parsed: AmendHistoricNonFhlUkPeriodSummaryRequestData): Validated[Seq[MtdError], AmendHistoricNonFhlUkPeriodSummaryRequestData] = {
        import parsed.body._

        List(
          income.map(validateIncome).getOrElse(valid),
          expenses.map(validateExpenses).getOrElse(valid)
        ).traverse(identity).map(_ => parsed)
      }

    }

  private def validateIncome(income: UkNonFhlPieIncome): Validated[Seq[MtdError], Unit] = {
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

  private def validateExpenses(expenses: UkNonFhlPieExpenses): Validated[Seq[MtdError], Unit] = {
    import expenses._

    val valuesWithPaths = List(
      (premisesRunningCosts, "/expenses/premisesRunningCosts"),
      (repairsAndMaintenance, "/expenses/repairsAndMaintenance"),
      (financialCosts, "/expenses/premisesCosts"),
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

    val validatedBothExpenses = consolidatedExpenses match {
      case None => valid
      case Some(_) =>
        expenses match {
          case UkNonFhlPieExpenses(None, None, None, None, None, None, Some(_), None, None, None, None) => valid
          case _ => Invalid(List(RuleBothExpensesSuppliedError.withPath("/expenses/consolidatedExpenses")))
        }
    }

    (validatedNumberFields :+ validatedBothExpenses).sequence.andThen(_ => valid)

  }

}
