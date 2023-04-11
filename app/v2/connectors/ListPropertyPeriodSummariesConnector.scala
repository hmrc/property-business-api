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

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequest
import v2.models.response.listPropertyPeriodSummaries.ListPropertyPeriodSummariesResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPropertyPeriodSummariesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listPeriodSummaries(request: ListPropertyPeriodSummariesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListPropertyPeriodSummariesResponse]] = {

    import request._

    val (downstreamUri, queryParams) = if (taxYear.useTaxYearSpecificApi) {
      (
        TaxYearSpecificIfsUri[ListPropertyPeriodSummariesResponse](
          s"income-tax/business/property/${taxYear.asTysDownstream}/$nino/$businessId/period"),
        Nil
      )
    } else {
      (
        IfsUri[ListPropertyPeriodSummariesResponse](s"income-tax/business/property/$nino/$businessId/period"),
        // Note that MTD tax year format is used
        List("taxYear" -> taxYear.asMtd)
      )
    }

    get(downstreamUri, queryParams)
  }

}
