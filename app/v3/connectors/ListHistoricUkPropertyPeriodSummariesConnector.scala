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

package v3.connectors

import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.HistoricPropertyType
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v3.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData
import v3.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListHistoricUkPropertyPeriodSummariesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listPeriodSummaries(request: ListHistoricUkPropertyPeriodSummariesRequestData, propertyType: HistoricPropertyType)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = {

    import request._

    val propertyTypeName = propertyType match {
      case HistoricPropertyType.Fhl    => "furnished-holiday-lettings"
      case HistoricPropertyType.NonFhl => "other"
    }

    val downstreamUri = IfsUri[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]](
      s"income-tax/nino/$nino/uk-properties/$propertyTypeName/periodic-summaries")

    get(downstreamUri)
  }

}
