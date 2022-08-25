/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.connectors

import config.AppConfig
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardIfsHttpParser._
import v2.models.domain.HistoricPropertyType
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequest
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ListHistoricUkPropertyPeriodSummariesConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listPeriodSummaries(request: ListHistoricUkPropertyPeriodSummariesRequest, propertyType: HistoricPropertyType)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = {

    val propertyTypeName = propertyType match {
      case HistoricPropertyType.Fhl    => "furnished-holiday-lettings"
      case HistoricPropertyType.NonFhl => "other"
    }

    val url = s"income-tax/nino/${request.nino.nino}/uk-properties/$propertyTypeName/periodic-summaries"

    get(
      uri = IfsUri[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]](url)
    )
  }
}
