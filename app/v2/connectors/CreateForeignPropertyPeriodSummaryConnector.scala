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

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequestData
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse]] = {

    import request._

    implicit val successCode: SuccessCode = SuccessCode(Status.OK)

    val downstreamUri = if (taxYear.isTys) {
      TaxYearSpecificIfsUri[CreateForeignPropertyPeriodSummaryResponse](
        s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId"
      )
    } else {
      IfsUri[CreateForeignPropertyPeriodSummaryResponse](
        // Note that MTD tax year format is used
        s"income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId"
      )
    }

    post(body, downstreamUri)
  }

}
