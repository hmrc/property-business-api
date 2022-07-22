/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse
import v2.connectors.DownstreamUri.DesUri
import javax.inject.{Inject, Singleton}
import v2.connectors.httpparsers.StandardIfsHttpParser._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector @Inject()(val http: HttpClient,
                                                                          val appConfig: AppConfig) extends BaseDownstreamConnector {


  def retrieve(request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest)(implicit hc: HeaderCarrier,
                                                                                    ec: ExecutionContext,
                                                                                    correlationId: String): Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]] = {
    val nino = request.nino.nino
    val taxYear = request.taxYear
    val response = get(
      uri = DesUri[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]
        (s"income-tax/nino/$nino/uk-properties/other/annual-summaries/$taxYear")
    )

    response
  }

}
