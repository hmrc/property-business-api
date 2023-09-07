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
import api.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import api.controllers.requestParsers.validators.validations.NumberValidation.validateOptional
import api.controllers.requestParsers.validators.validations.{HistoricPeriodIdValidation, NinoValidation}
import api.models.errors.MtdError
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.ConsolidatedExpensesValidation
import v2.models.request.amendHistoricFhlUkPiePeriodSummary.{AmendHistoricFhlUkPiePeriodSummaryRawData, AmendHistoricFhlUkPiePeriodSummaryRequestBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendHistoricFhlUkPiePeriodSummaryValidator @Inject() (appConfig: AppConfig) extends Validator[AmendHistoricFhlUkPiePeriodSummaryRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  lazy private val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: AmendHistoricFhlUkPiePeriodSummaryRawData): List[MtdError] = {
    (for {
      _    <- validatePathParameters(data)
      body <- validateAndCheckNonEmptyOrRead[AmendHistoricFhlUkPiePeriodSummaryRequestBody](data.body)
      _    <- validateBody(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParameters(data: AmendHistoricFhlUkPiePeriodSummaryRawData): Either[List[MtdError], Unit] = {
    val ninoError =
      NinoValidation.validate(data.nino)

    val periodIdError =
      HistoricPeriodIdValidation.validatePeriodId(minTaxYear, maxTaxYear, data.periodId)

    errorsResult(ninoError ++ periodIdError)
  }

  private def validateBody(body: AmendHistoricFhlUkPiePeriodSummaryRequestBody): Either[List[MtdError], Unit] = {

    val incomeFormatErrors = body.income
      .map { income =>
        import income._
        validateOptional(periodAmount, "/income/periodAmount") ++
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
          validateOptional(rentARoom.flatMap(_.amountClaimed), "/income/rentARoom/amountClaimed")
      }
      .getOrElse(Nil)

    val bothExpensesErrors = body.expenses.map(ConsolidatedExpensesValidation.validate(_, "/expenses/consolidatedExpenses")).getOrElse(Nil)

    errorsResult(incomeFormatErrors ++ expensesFormatErrors ++ bothExpensesErrors)
  }

}
