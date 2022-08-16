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

import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardIfsHttpParser._
import v2.models.request.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionRequest

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AmendUkPropertyAnnualSubmissionConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def amendUkPropertyAnnualSubmission(request: AmendUkPropertyAnnualSubmissionRequest)(implicit hc: HeaderCarrier,
                                                                                       ec: ExecutionContext,
                                                                                       correlationId: String): Future[DownstreamOutcome[Unit]] = {

    put(
      body = request.body,
      uri = IfsUri[Unit](
        s"income-tax/business/property/annual?taxableEntityId=${request.nino.nino}&incomeSourceId=${request.businessId}&taxYear=${request.taxYear}")
    )
  }
}
