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

package v4.controllers.createUkPropertyPeriodSummary

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.AppConfig
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.controllers.createUkPropertyPeriodSummary.model.request.{
  CreateUkPropertyPeriodSummaryRequestData,
  Def1_CreateUkPropertyPeriodSummaryRequestData,
  Def2_CreateUkPropertyPeriodSummaryRequestData
}
import v4.controllers.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createUkProperty(request: CreateUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)
    request match {
      case def1: Def1_CreateUkPropertyPeriodSummaryRequestData =>
        import def1._
        val downstreamUri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] = if (taxYear.isNotTY25) {
          TaxYearSpecificIfsUri(s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
        } else {
          // Note that MTD tax year format is used pre-TYS
          IfsUri(s"income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId")
        }
        post(body, downstreamUri)

      case def2: Def2_CreateUkPropertyPeriodSummaryRequestData =>
        import def2._
        val downstreamUri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] = TaxYearSpecificIfsUri(
          s"income-tax/business/property/periodic/24-25?taxableEntityId=$nino&incomeSourceId=$businessId")
        post(body, downstreamUri)
    }
  }

}
