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

package v6.createUkPropertyPeriodSummary

import play.api.http.Status.OK
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.createUkPropertyPeriodSummary.model.request.{
  CreateUkPropertyPeriodSummaryRequestData,
  Def1_CreateUkPropertyPeriodSummaryRequestData,
  Def2_CreateUkPropertyPeriodSummaryRequestData,
  Def2_CreateUkPropertyPeriodSummarySubmissionRequestData
}
import v6.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def createUkProperty(request: CreateUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)
    request match {
      case def1: Def1_CreateUkPropertyPeriodSummaryRequestData =>
        import def1.*
        val downstreamUri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] = if (taxYear.useTaxYearSpecificApi) {
          if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1861")) {
            HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/periodic/$nino/$businessId")
          } else {
            IfsUri(s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
          }
        } else {
          // Note that MTD tax year format is used pre-TYS
          IfsUri(s"income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId")
        }
        post(body, downstreamUri)

      case def2: Def2_CreateUkPropertyPeriodSummaryRequestData =>
        import def2.*
        val downstreamUri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] =
          if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1861")) {
            HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/periodic/$nino/$businessId")
          } else {
            IfsUri(s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
          }
        post(body, downstreamUri)

      case def2Submission: Def2_CreateUkPropertyPeriodSummarySubmissionRequestData =>
        import def2Submission.*
        val downstreamUri: DownstreamUri[CreateUkPropertyPeriodSummaryResponse] =
          if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1861")) {
            HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/periodic/$nino/$businessId")
          } else {
            IfsUri(s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId")
          }
        post(body, downstreamUri)
    }
  }

}
