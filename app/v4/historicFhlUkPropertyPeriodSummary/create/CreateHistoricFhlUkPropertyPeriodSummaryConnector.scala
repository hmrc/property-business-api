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

package v4.historicFhlUkPropertyPeriodSummary.create

import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.historicFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricFhlUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def create(request: CreateHistoricFhlUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(Status.OK)

    request match {
      case def1: Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData =>
        import def1._

        val downstreamUri = IfsUri[Unit](s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries")

        post(body, downstreamUri)
    }

  }

}
