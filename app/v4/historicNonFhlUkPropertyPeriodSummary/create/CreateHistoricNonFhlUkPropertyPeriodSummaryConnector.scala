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

package v4.historicNonFhlUkPropertyPeriodSummary.create

import play.api.http.Status
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.historicNonFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricNonFhlUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  implicit val successCode: SuccessCode = SuccessCode(Status.OK)

  def createPeriodSummary(request: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ex: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    request match {
      case def1: Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData =>
        import def1.*
        val downstreamUri = IfsUri[Unit](s"income-tax/nino/$nino/uk-properties/other/periodic-summaries")
        post(body, downstreamUri)

    }

  }

}
