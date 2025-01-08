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

package v4.amendUkPropertyPeriodSummary

import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.config.SharedAppConfig
import play.api.http.Status.NO_CONTENT
import shared.models.domain.TaxYear
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.amendUkPropertyPeriodSummary.model.request.{
  AmendUkPropertyPeriodSummaryRequestData,
  Def1_AmendUkPropertyPeriodSummaryRequestData,
  Def2_AmendUkPropertyPeriodSummaryRequestData,
  Def2_AmendUkPropertyPeriodSummarySubmissionRequestData
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def amendUkPropertyPeriodSummary(request: AmendUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    request match {
      case def1: Def1_AmendUkPropertyPeriodSummaryRequestData =>
        import def1._
        val downstreamUri = if (taxYear.year >= TaxYear.tysTaxYear.year) {
          TaxYearSpecificIfsUri[Unit](
            s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId")
        } else
          IfsUri[Unit](
            s"income-tax/business/property/periodic?" + s"taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId")
        put(body, downstreamUri)

      case def2: Def2_AmendUkPropertyPeriodSummaryRequestData =>
        import def2._
        val downstreamUri = TaxYearSpecificIfsUri[Unit](
          s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId")
        put(body, downstreamUri)

      case def2Submission: Def2_AmendUkPropertyPeriodSummarySubmissionRequestData =>
        import def2Submission._
        val downstreamUri = TaxYearSpecificIfsUri[Unit](
          s"income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId")
        put(body, downstreamUri)

    }
  }

}
