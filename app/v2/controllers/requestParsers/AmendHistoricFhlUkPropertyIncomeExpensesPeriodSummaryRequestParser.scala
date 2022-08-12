package v2.controllers.requestParsers

import javax.inject.Inject

class AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestParser @Inject()(
    val validator: AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator)
    extends RequestParser[AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData, AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequest] {

  override protected def requestFor(
      data: AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData): AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequest =
    AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequest(Nino(data.nino),
                                                                 data.periodId,
                                                                 data.body.as[AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestBody])
}
