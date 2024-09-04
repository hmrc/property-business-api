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

package v4.historicFhlUkPropertyPeriodSummary.retrieve

import api.connectors.DownstreamUri.DesUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.request._
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricFhlUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveHistoricFhlUkPropertyPeriodSummaryResponse]] = {
    request match {
      case def1: Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData =>
        import def1._

        val downstreamUri = DesUri[Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse](
          s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summary-detail?from=${periodId.from}&to=${periodId.to}")

        val result = get(downstreamUri)
        result
    }

  }

}
