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
import play.api.http.Status.OK
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryRequest

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AmendHistoricNonFhlUkPiePeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  implicit val successCode: SuccessCode = SuccessCode(OK)

  def amend(request: AmendHistoricNonFhlUkPiePeriodSummaryRequest)(implicit hc: HeaderCarrier,
                                                                   ec: ExecutionContext,
                                                                   correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val path =
      s"income-tax/nino/${request.nino.value}/uk-properties/other/periodic-summaries" +
        s"?from=${request.periodId.from}" +
        s"&to=${request.periodId.to}"

    put(
      body = request.body,
      uri = IfsUri[Unit](path)
    )
  }
}
