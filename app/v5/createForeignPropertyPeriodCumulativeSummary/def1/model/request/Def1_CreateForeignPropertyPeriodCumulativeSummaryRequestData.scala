
package v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request

import api.models.domain.{BusinessId, Nino, TaxYear}
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestData

case class Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(nino: Nino,
                                                                        businessId: BusinessId,
                                                                        taxYear: TaxYear,
                                                                        body: Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody)
  extends CreateForeignPropertyPeriodCumulativeSummaryRequestData

