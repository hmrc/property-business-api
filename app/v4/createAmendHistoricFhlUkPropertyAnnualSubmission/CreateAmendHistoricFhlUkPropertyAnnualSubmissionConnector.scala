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

package v4.createAmendHistoricFhlUkPropertyAnnualSubmission

import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request.{
  CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData,
  Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData
}
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)
    extends BaseDownstreamConnector {

  def amend(request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse]] = {

    request match {
      case def1: Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData =>
        import def1._

        val downstreamUri = IfsUri[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse](
          s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/${taxYear.asDownstream}")

        put(body, downstreamUri)
    }

  }

}
