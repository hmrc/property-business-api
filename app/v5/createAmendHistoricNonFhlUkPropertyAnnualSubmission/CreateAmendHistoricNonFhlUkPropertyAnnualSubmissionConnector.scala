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

package v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import play.api.libs.json.Format.GenericFormat
import shared.config.SharedAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def amend(request: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse]] = {

    import request._

    val downstreamUri = IfsUri[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse](
      s"income-tax/nino/$nino/uk-properties/other/annual-summaries/${taxYear.asDownstream}")

    request match {
      case def1: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData =>
        put(def1.body, downstreamUri)
    }

  }

}
