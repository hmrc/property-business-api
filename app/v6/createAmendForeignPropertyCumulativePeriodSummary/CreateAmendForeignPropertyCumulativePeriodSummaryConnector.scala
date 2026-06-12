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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import api.config.SharedAppConfig
import api.connectors.*
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendForeignPropertyCumulativePeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def createAmendForeignProperty(request: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request.*

    lazy val downstreamUri: DownstreamUri[Unit] = if (taxYear.year >= 2027) {
      HipUri[Unit](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/periodic/foreign-property/$nino/$businessId")
    } else {
      HipUri[Unit](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/periodic/property/$nino/$businessId")
    }

    put(body, downstreamUri)
  }

}
