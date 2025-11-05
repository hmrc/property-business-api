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

package v6.createForeignPropertyDetails

import shared.config.SharedAppConfig
import shared.connectors.*
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.DownstreamUri.HipUri
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData
import v6.createForeignPropertyDetails.model.response.CreateForeignPropertyDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyDetailsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def createForeignPropertyDetails(request: CreateForeignPropertyDetailsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateForeignPropertyDetailsResponse]] = {

    import request.*
    import schema.*

    val downstreamUri: DownstreamUri[DownstreamResp] =
      HipUri(s"itsd/income-sources/$nino/foreign-property-details/$businessId?taxYear=${taxYear.asTysDownstream}")

    post(body, downstreamUri)
  }

}
