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
import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.deletePropertyAnnualSubmission.DeletePropertyAnnualSubmissionRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deletePropertyAnnualSubmission(request: DeletePropertyAnnualSubmissionRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val (downstreamUri, queryParams) =
      if (request.taxYear.useTaxYearSpecificApi) {
        (
          TaxYearSpecificIfsUri[Unit](
            s"income-tax/business/property/annual/${request.taxYear.asTysDownstream}/${request.nino.value}/${request.businessId}"),
          Nil
        )
      } else {
        (
          IfsUri[Unit](s"income-tax/business/property/annual"),
          List(
            "taxableEntityId" -> request.nino.nino,
            "incomeSourceId"  -> request.businessId,
            "taxYear"         -> request.taxYear.asMtd // Note that MTD tax year format is used
          )
        )
      }

    delete(downstreamUri, queryParams)
  }

}
