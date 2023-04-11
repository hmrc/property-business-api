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

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import config.AppConfig
import play.api.libs.json.Format.GenericFormat
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)
    extends BaseDownstreamConnector {

  def amend(request: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse]] = {

    import request._

    val downstreamUri = IfsUri[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse](
      s"income-tax/nino/$nino/uk-properties/other/annual-summaries/${taxYear.asDownstream}")

    put(body, downstreamUri)
  }

}
