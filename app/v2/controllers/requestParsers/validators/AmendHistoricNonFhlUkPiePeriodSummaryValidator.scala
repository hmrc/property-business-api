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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.NinoValidation
import api.models.errors.MtdError
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.{ConsolidatedExpensesValidation, HistoricPeriodIdValidation}
import v2.controllers.requestParsers.validators.validations.NumberValidation.validateOptional
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.{
  AmendHistoricNonFhlUkPiePeriodSummaryRawData,
  AmendHistoricNonFhlUkPiePeriodSummaryRequestBody
}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendHistoricNonFhlUkPiePeriodSummaryValidator @Inject() (appConfig: AppConfig)
    extends Validator[AmendHistoricNonFhlUkPiePeriodSummaryRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  lazy private val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: AmendHistoricNonFhlUkPiePeriodSummaryRawData): List[MtdError] = {
    (for {
      _    <- validatePathParameters(data)
      body <- validateAndCheckNonEmptyOrRead[AmendHistoricNonFhlUkPiePeriodSummaryRequestBody](data.body)
      _    <- validateBody(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParameters(data: AmendHistoricNonFhlUkPiePeriodSummaryRawData): Either[List[MtdError], Unit] = {
    val ninoError =
      NinoValidation.validate(data.nino)

    val periodIdError =
      HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, data.periodId)

    errorsResult(ninoError ++ periodIdError)
  }

  private def validateBody(body: AmendHistoricNonFhlUkPiePeriodSummaryRequestBody): Either[List[MtdError], Unit] = {

    val incomeFormatErrors = body.income
      .map { income =>
        import income._
        validateOptional(periodAmount, "/income/periodAmount") ++
          validateOptional(premiumsOfLeaseGrant, "/income/premiumsOfLeaseGrant") ++
          validateOptional(reversePremiums, "/income/reversePremiums") ++
          validateOptional(otherIncome, "/income/otherIncome") ++
          validateOptional(taxDeducted, "/income/taxDeducted") ++
          validateOptional(rentARoom.flatMap(_.rentsReceived), "/income/rentARoom/rentsReceived")
      }
      .getOrElse(Nil)

    val expensesFormatErrors = body.expenses
      .map { expenses =>
        import expenses._

        validateOptional(premisesRunningCosts, "/expenses/premisesRunningCosts") ++
          validateOptional(repairsAndMaintenance, "/expenses/repairsAndMaintenance") ++
          validateOptional(financialCosts, "/expenses/premisesCosts") ++
          validateOptional(professionalFees, "/expenses/professionalFees") ++
          validateOptional(costOfServices, "/expenses/costOfServices") ++
          validateOptional(other, "/expenses/other") ++
          validateOptional(consolidatedExpenses, "/expenses/consolidatedExpenses") ++
          validateOptional(travelCosts, "/expenses/travelCosts") ++
          validateOptional(residentialFinancialCostsCarriedForward, "/expenses/residentialFinancialCostsCarriedForward") ++
          validateOptional(residentialFinancialCost, "/expenses/residentialFinancialCost") ++
          validateOptional(rentARoom.flatMap(_.amountClaimed), "/income/rentARoom/amountClaimed")
      }
      .getOrElse(Nil)

    val bothExpensesErrors = body.expenses.map(ConsolidatedExpensesValidation.validate(_, "/expenses/consolidatedExpenses")).getOrElse(Nil)

    errorsResult(incomeFormatErrors ++ expensesFormatErrors ++ bothExpensesErrors)
  }

}
