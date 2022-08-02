/*
 * Copyright 2022 HM Revenue & Customs
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
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.NumberValidation.{ validateOptional => optionalNumber }
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors.MtdError

import javax.inject.Singleton

@Singleton
class CreateHistoricNonFhlUkPropertyPeriodSummaryValidator @Inject()(appConfig: AppConfig)
    extends Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  private lazy val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: CreateHistoricNonFhlUkPropertyPeriodSummaryRawData): List[MtdError] = {
    (for {
      _    <- validatePathParams(data)
      body <- validateAndCheckNonEmptyOrRead[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody](data.body)
      _    <- validateBody(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParams(data: CreateHistoricNonFhlUkPropertyPeriodSummaryRawData): Either[List[MtdError], Unit] = {
    errorsResult(NinoValidation.validate(data.nino))
  }

  private def validateBody(body: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody): Either[List[MtdError], Unit] = {
    val fromDateValidationErrors = DateValidation.validate(body.fromDate, true)
    val toDateValidationErrors   = DateValidation.validate(body.toDate, false)

    val incomeErrors = body.income
      .map { income =>
        import income._

        optionalNumber(periodAmount, "/income/periodAmount") ++
          optionalNumber(taxDeducted, "/income/taxDeducted") ++
          optionalNumber(premiumsOfLeaseGrant, "/income/premiumsOfLeaseGrant") ++
          optionalNumber(reversePremiums, "/income/reversePremiums") ++
          optionalNumber(otherIncome, "/income/otherIncome") ++
          optionalNumber(rentARoom, "/income/rentARoom") ++
          optionalNumber(rentsReceived, "/income/rentARoom/rentsReceived")
      }
      .getOrElse(Nil)

    val expensesErrors = body.expenses
      .map { expenses =>
        import expenses._

        optionalNumber(premisesRunningCosts, "/expenses/premisesRunningCosts") ++
          optionalNumber(repairsAndMaintenance, "/expenses/repairsAndMaintenance") ++
          optionalNumber(financialCosts, "/expenses/premisesCosts") ++
          optionalNumber(professionalFees, "/expenses/professionalFees") ++
          optionalNumber(costOfServices, "/expenses/costOfServices") ++
          optionalNumber(other, "/expenses/other") ++
          optionalNumber(consolidatedExpenses, "/expenses/consolidatedExpenses") ++
          optionalNumber(travelCosts, "/expenses/travelCosts") ++
          optionalNumber(residentialFinancialCostsCarriedForward, "/expenses/residentialFinancialCostsCarriedForward") ++
          optionalNumber(residentialFinancialCost, "/expenses/residentialFinancialCost") ++
          optionalNumber(rentARoom, "/expenses/rentARoom") ++
          optionalNumber(amountClaimed, "/expenses/rentARoom/amountClaimed")

      }
      .getOrElse(Nil)

    errorsResult(fromDateValidationErrors ++ toDateValidationErrors ++ incomeErrors ++ expensesErrors)
  }
}
