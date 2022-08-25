/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.request.listHistoricUkPropertyPeriodSummaries

import v2.models.domain.Nino

case class ListHistoricUkPropertyPeriodSummariesRawData(nino: String)

case class ListHistoricUkPropertyPeriodSummariesRequest(nino: Nino)
