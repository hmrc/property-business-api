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

package v2.connectors
import config.AppConfig
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.DesUri
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryResponse

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveHistoricNonFhlUkPiePeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]] = {

    val nino     = request.nino.value
    val periodId = request.periodId

    get(
      uri = DesUri[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse](
        s"income-tax/nino/$nino/uk-properties/other/periodic-summary-detail?from=${periodId.from}&to=${periodId.to}")
    )

  }

}
