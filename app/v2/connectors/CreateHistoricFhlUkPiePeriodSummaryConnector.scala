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
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardIfsHttpParser._
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRequest
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryResponse

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createPeriodSummary(request: CreateHistoricFhlUkPiePeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ex: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse]] = {

    val outcomeF = post(
      body = request.body,
      uri = IfsUri[CreateHistoricFhlUkPiePeriodSummaryResponse](
        s"income-tax/nino/${request.nino.nino}/uk-properties/furnished-holiday-lettings/periodic-summaries")
    )
    val result: Future[DownstreamOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse]] = {
      outcomeF.map(_.map { wrapper =>
        val periodId = s"${request.body.fromDate}_${request.body.toDate}"
        wrapper.copy(responseData = wrapper.responseData.copy(periodId = Some(periodId)))
      })
    }
    result
  }
}
