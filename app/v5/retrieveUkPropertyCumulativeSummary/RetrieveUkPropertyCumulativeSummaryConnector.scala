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

package v5.retrieveUkPropertyCumulativeSummary

import api.connectors.DownstreamUri.TaxYearSpecificIfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.{AppConfig, FeatureSwitches}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v5.retrieveUkPropertyCumulativeSummary.RetrieveUkPropertyCumulativeSummaryConnector._
import v5.retrieveUkPropertyCumulativeSummary.model.request._
import v5.retrieveUkPropertyCumulativeSummary.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveUkPropertyCumulativeSummaryConnector {

  sealed trait Result

  case class UkResult(response: RetrieveUkPropertyCumulativeSummaryResponse) extends Result

  case object NonUkResult extends Result
}

@Singleton
class RetrieveUkPropertyCumulativeSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkPropertyCumulativeSummary(request: RetrieveUkPropertyCumulativeSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request._
    import schema._

    val maybeIntent = if (FeatureSwitches(appConfig).isPassIntentEnabled) Some("UK_PROPERTY") else None

    val downstreamUri: DownstreamUri[DownstreamResp] = TaxYearSpecificIfsUri[DownstreamResp](
      s"income-tax/${taxYear.asTysDownstream}/business/property/periodic/${nino.value}/${businessId.businessId}")

    get(uri = downstreamUri, maybeIntent = maybeIntent)
      .map(_.map(_.map { response => if (response.hasUkData) UkResult(response) else NonUkResult }))
  }

}
