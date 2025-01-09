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

package v4.historicFhlUkPropertyPeriodSummary.list

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.historicFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v4.historicFhlUkPropertyPeriodSummary.list.model.request.{
  Def1_ListHistoricFhlUkPropertyPeriodSummariesRequestData,
  ListHistoricFhlUkPropertyPeriodSummariesRequestData
}
import v4.historicFhlUkPropertyPeriodSummary.list.model.response.ListHistoricFhlUkPropertyPeriodSummariesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListHistoricFhlUkPropertyPeriodSummariesConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def listPeriodSummaries(
      request: ListHistoricFhlUkPropertyPeriodSummariesRequestData
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String
  ): Future[DownstreamOutcome[ListHistoricFhlUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = {

    request match {
      case def1: Def1_ListHistoricFhlUkPropertyPeriodSummariesRequestData =>
        import def1._

        val downstreamUri = IfsUri[ListHistoricFhlUkPropertyPeriodSummariesResponse[SubmissionPeriod]](
          s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries")

        get(downstreamUri)
    }
  }

}
