package v2.controllers.requestParsers.validators

import config.AppConfig
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.{
  ConsolidatedExpensesValidation,
  HistoricPeriodIdValidation,
  NinoValidation,
  PeriodIdValidation
}
import v2.controllers.requestParsers.validators.validations.NumberValidation.validateOptional
import v2.models.errors.MtdError

import javax.inject.{ Inject, Singleton }

@Singleton
class AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator @Inject()(appConfig: AppConfig)
    extends Validator[AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  lazy private val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData): List[MtdError] = {
    (for {
      _    <- validatePathParameters(data)
      body <- validateAndCheckNonEmptyOrRead[AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestBody](data.body)
      _    <- validateBody(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParameters(data: AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData): Either[List[MtdError], Unit] = {
    val ninoError =
      NinoValidation.validate(data.nino)

    val periodIdError =
      HistoricPeriodIdValidation.validate(minTaxYear, maxTaxYear, data.periodId)

    errorsResult(ninoError ++ periodIdError)
  }

  private def validateBody(body: AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestBody): Either[List[MtdError], Unit] = {

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
