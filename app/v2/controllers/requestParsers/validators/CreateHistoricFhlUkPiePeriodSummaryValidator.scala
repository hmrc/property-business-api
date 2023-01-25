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

import com.google.inject.Inject
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.NumberValidation.validateOptional
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors.MtdError
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{
  CreateHistoricFhlUkPiePeriodSummaryRawData,
  CreateHistoricFhlUkPiePeriodSummaryRequestBody
}

import javax.inject.Singleton

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryValidator @Inject() extends Validator[CreateHistoricFhlUkPiePeriodSummaryRawData] {

  override def validate(data: CreateHistoricFhlUkPiePeriodSummaryRawData): List[MtdError] = {
    (for {
      _    <- validatePathParams(data)
      body <- validateAndCheckNonEmptyOrRead[CreateHistoricFhlUkPiePeriodSummaryRequestBody](data.body)
      _    <- validateBody(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParams(data: CreateHistoricFhlUkPiePeriodSummaryRawData): Either[List[MtdError], Unit] = {
    val ninoError = NinoValidation.validate(data.nino)
    errorsResult(ninoError)
  }

  private def validateBody(body: CreateHistoricFhlUkPiePeriodSummaryRequestBody): Either[List[MtdError], Unit] = {

    val formatDateErrors =
      DateValidation.validate(body.fromDate, isFromDate = true) ++
        DateValidation.validate(body.toDate, isFromDate = false)

    def validateToDateIsAfterFromDate: List[MtdError] =
      if (formatDateErrors.isEmpty)
        ToDateBeforeFromDateValidation.validate(from = body.fromDate, to = body.toDate)
      else
        NoValidationErrors

    val ruleDateErrors = validateToDateIsAfterFromDate

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
          validateOptional(financialCosts, "/expenses/financialCosts") ++
          validateOptional(professionalFees, "/expenses/professionalFees") ++
          validateOptional(costOfServices, "/expenses/costOfServices") ++
          validateOptional(other, "/expenses/other") ++
          validateOptional(consolidatedExpenses, "/expenses/consolidatedExpenses") ++
          validateOptional(travelCosts, "/expenses/travelCosts") ++
          validateOptional(rentARoom.flatMap(_.amountClaimed), "/expenses/rentARoom/amountClaimed")
      }
      .getOrElse(Nil)

    val bothExpensesErrors = body.expenses.map(ConsolidatedExpensesValidation.validate(_, "/expenses/consolidatedExpenses")).getOrElse(Nil)

    errorsResult(formatDateErrors ++ ruleDateErrors ++ incomeFormatErrors ++ expensesFormatErrors ++ bothExpensesErrors)
  }
}
