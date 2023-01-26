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

import config.AppConfig
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.domain.HistoricPropertyType
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequest

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deleteHistoricUkPropertyAnnualSubmission(request: DeleteHistoricUkPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear.asDownstream

    val propertyTypeName = request.propertyType match {
      case HistoricPropertyType.Fhl    => "furnished-holiday-lettings"
      case HistoricPropertyType.NonFhl => "other"
    }

    put(
      body = JsObject.empty,
      uri = IfsUri[Unit](
        s"income-tax/nino/$nino/uk-properties/$propertyTypeName/annual-summaries/$taxYear"
      )
    )
  }
}
