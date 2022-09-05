/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.connectors
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.connectors.DownstreamUri.DesUri
import v2.connectors.httpparsers.StandardIfsHttpParser._
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricFhlUkPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig)
  extends BaseDownstreamConnector {

  def retrieve(request: RetrieveHistoricFhlUkPiePeriodSummaryRequest)(
  implicit hc: HeaderCarrier,
  ec: ExecutionContext,
  correlationId: String): Future[DownstreamOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse]] = {

  val nino    = request.nino.value
  val periodId = request.periodId

    val response = get(
      uri = DesUri[RetrieveHistoricFhlUkPiePeriodSummaryResponse](
        s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summary-detail?from=${periodId.from}&to=${periodId.to}")
    )

  response

    }

  }