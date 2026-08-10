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

package v6.deletePropertyAnnualSubmission

import api.config.{AppConfig, ConfigFeatureSwitches}
import api.connectors.DownstreamUri.{HipUri, IfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.deletePropertyAnnualSubmission.model.request.DeletePropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deletePropertyAnnualSubmission(request: DeletePropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request.*

    val queryParams = List(
      "taxableEntityId" -> nino.nino,
      "incomeSourceId"  -> businessId.businessId,
      "taxYear"         -> taxYear.asMtd
    )

    lazy val downstreamUri1596 = IfsUri[Unit](s"income-tax/business/property/annual")
    lazy val downstreamUri1863 = if (taxYear.year >= 2026 && ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1863")) {
      HipUri[Unit](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId")
    } else {
      IfsUri[Unit](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId")
    }

    if (taxYear.useTaxYearSpecificApi) delete(downstreamUri1863) else delete(downstreamUri1596, queryParams)
  }

}
