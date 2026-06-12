/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.retrieveUkPropertyCumulativeSummary

import api.config.AppConfig
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.PropertyBusinessFeatureSwitches
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.retrieveUkPropertyCumulativeSummary.model.request.*
import v6.retrieveUkPropertyCumulativeSummary.model.{NonUkResult, Result, UkResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkPropertyCumulativeSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkPropertyCumulativeSummary(request: RetrieveUkPropertyCumulativeSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request.*
    import schema.*

    val maybeIntent = if (PropertyBusinessFeatureSwitches().isPassIntentEnabled) Some("UK_PROPERTY") else None

    val downstreamUri =
      HipUri[DownstreamResp](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/periodic/property/${nino.value}/${businessId.businessId}")

    get(uri = downstreamUri, maybeIntent = maybeIntent)
      .map(_.map(_.map { response => if (response.hasUkData) UkResult(response) else NonUkResult }))
  }

}
