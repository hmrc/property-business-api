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

package v6.historicNonFhlUkPropertyPeriodSummary.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.DesUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.historicNonFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData
}
import v6.historicNonFhlUkPropertyPeriodSummary.retrieve.model.response.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse]] = {

    request match {
      case def1: Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData =>
        import def1._

        val downstreamUri = DesUri[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse](
          s"income-tax/nino/$nino/uk-properties/other/periodic-summary-detail?from=${periodId.from}&to=${periodId.to}")

        val result = get(downstreamUri)
        result
    }
  }

}
