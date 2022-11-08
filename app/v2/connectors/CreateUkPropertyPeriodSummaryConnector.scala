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

import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.{ IfsUri, TaxYearSpecificIfsUri }
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.request.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryRequest
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateUkPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createUkProperty(request: CreateUkPropertyPeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse]] = {

    import request._

    val uri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri(
        s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=${nino.nino}&incomeSourceId=${request.businessId}")
    } else {
      // Note that MTD tax year format is used pre-TYS
      IfsUri(s"income-tax/business/property/periodic?taxableEntityId=${nino.nino}&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId")
    }

    post(body, uri)
  }
}
