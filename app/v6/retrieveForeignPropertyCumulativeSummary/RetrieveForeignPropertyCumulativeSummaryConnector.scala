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

package v6.retrieveForeignPropertyCumulativeSummary

import config.PropertyBusinessFeatureSwitches
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.*
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.model.{ForeignResult, NonForeignResult, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyCumulativeSummaryConnector @Inject() (val http: HttpClientV2)(implicit val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieveForeignPropertyCumulativeSummary(request: RetrieveForeignPropertyCumulativeSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request.*
    import schema.*

    val queryParams: Seq[(String, String)] =
      propertyId.map(pid => "propertyId" -> pid.propertyId).toSeq
    val maybeIntent = Option.when(PropertyBusinessFeatureSwitches().isPassIntentEnabled && taxYear.year == 2026)("FOREIGN_PROPERTY")

    lazy val downstreamUriForTy2627Onwards: DownstreamUri[DownstreamResp] = HipUri[DownstreamResp](
      s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/periodic/foreign-property/${nino.value}/${businessId.businessId}")

    lazy val downstreamUri1962: DownstreamUri[DownstreamResp] = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1962")) {
      HipUri[DownstreamResp](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/periodic/property/${nino.value}/${businessId.businessId}")
    } else {
      IfsUri[DownstreamResp](s"income-tax/${taxYear.asTysDownstream}/business/property/periodic/${nino.value}/${businessId.businessId}")
    }

    val downstreamUri: DownstreamUri[DownstreamResp] = if (taxYear.year >= 2027) downstreamUriForTy2627Onwards else downstreamUri1962

    get(uri = downstreamUri, queryParams, maybeIntent = maybeIntent)
      .map(_.map(_.map { response => if (response.hasForeignData) ForeignResult(response) else NonForeignResult }))
  }

}
