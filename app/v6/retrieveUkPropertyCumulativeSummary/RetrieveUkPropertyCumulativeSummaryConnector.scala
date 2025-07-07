/*
 * Copyright 2025 HM Revenue & Customs
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

import config.PropertyBusinessFeatureSwitches
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.retrieveUkPropertyCumulativeSummary.RetrieveUkPropertyCumulativeSummaryConnector._
import v6.retrieveUkPropertyCumulativeSummary.model.request._
import v6.retrieveUkPropertyCumulativeSummary.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveUkPropertyCumulativeSummaryConnector {

  sealed trait Result

  case class UkResult(response: RetrieveUkPropertyCumulativeSummaryResponse) extends Result

  case object NonUkResult extends Result
}

@Singleton
class RetrieveUkPropertyCumulativeSummaryConnector @Inject()(val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveUkPropertyCumulativeSummary(request: RetrieveUkPropertyCumulativeSummaryRequestData)(implicit
                                                                                                   hc: HeaderCarrier,
                                                                                                   ec: ExecutionContext,
                                                                                                   correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request._
    import schema._

    val maybeIntent = if (PropertyBusinessFeatureSwitches().isPassIntentEnabled) Some("UK_PROPERTY") else None

    val downstreamUri: DownstreamUri[DownstreamResp] = IfsUri[DownstreamResp](
      s"income-tax/${taxYear.asTysDownstream}/business/property/periodic/${nino.value}/${businessId.businessId}")

    get(uri = downstreamUri, maybeIntent = maybeIntent)
      .map(_.map(_.map { response => if (response.hasUkData) UkResult(response) else NonUkResult }))
  }

}
