/*
 * Copyright 2025 HM Revenue & Customs
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

package v5.createForeignPropertyPeriodSummary

import play.api.http.Status.OK
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v5.createForeignPropertyPeriodSummary.model.request.{
  CreateForeignPropertyPeriodSummaryRequestData,
  Def1_CreateForeignPropertyPeriodSummaryRequestData,
  Def2_CreateForeignPropertyPeriodSummaryRequestData
}
import v5.createForeignPropertyPeriodSummary.model.response.CreateForeignPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)

    request match {
      case def1: Def1_CreateForeignPropertyPeriodSummaryRequestData =>
        import def1._
        val downstreamUri =
          if (taxYear.useTaxYearSpecificApi) {
            TaxYearSpecificIfsUri[CreateForeignPropertyPeriodSummaryResponse](
              s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
          } else {
            IfsUri[CreateForeignPropertyPeriodSummaryResponse](
              s"income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId")
          }

        post(body, downstreamUri)

      case def2: Def2_CreateForeignPropertyPeriodSummaryRequestData =>
        import def2._
        val downstreamUri = TaxYearSpecificIfsUri[CreateForeignPropertyPeriodSummaryResponse](
          s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
        post(body, downstreamUri)
    }
  }

}
