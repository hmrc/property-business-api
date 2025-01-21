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

package v6.retrieveHistoricFhlUkPropertyAnnualSubmission

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request._
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]] = {

    request match {
      case def1: Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData =>
        import def1._
        val downstreamUri =
          IfsUri[Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse](
            s"income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/${taxYear.asDownstream}")

        val result = get(downstreamUri)
        result
    }

  }

}
