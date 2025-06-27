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

package v4.historicFhlUkPropertyPeriodSummary.amend

import play.api.http.Status.OK
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.historicFhlUkPropertyPeriodSummary.amend.request.{
  AmendHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendHistoricFhlUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def amend(request: AmendHistoricFhlUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)

    request match {
      case def1: Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData =>
        import def1._

        val downstreamUri =
          IfsUri[Unit](
            s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries" +
              s"?from=${periodId.from}" +
              s"&to=${periodId.to}")

        put(body, downstreamUri)
    }

  }

}
