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

package v5.createForeignPropertyPeriodCumulativeSummary

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestData
import v5.createForeignPropertyPeriodCumulativeSummary.model.response.CreateForeignPropertyPeriodCumulativeSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodCumulativeSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)
    extends BaseDownstreamConnector {

  def createForeignProperty(request: CreateForeignPropertyPeriodCumulativeSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateForeignPropertyPeriodCumulativeSummaryResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)

    val downstreamUri = if (request.taxYear.isTys) {
      TaxYearSpecificIfsUri[CreateForeignPropertyPeriodCumulativeSummaryResponse](
        s"income-tax/business/property/periodic/${request.taxYear.asTysDownstream}?taxableEntityId=${request.nino}&incomeSourceId=${request.businessId}")
    } else {
      IfsUri[CreateForeignPropertyPeriodCumulativeSummaryResponse](
        s"income-tax/business/property/periodic?taxableEntityId=${request.nino}&taxYear=${request.taxYear.asMtd}&incomeSourceId=${request.businessId}")
    }

    post(request.body, downstreamUri)
  }

}
