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

package v6.deletePropertyAnnualSubmission

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v6.deletePropertyAnnualSubmission.model.request.{Def1_DeletePropertyAnnualSubmissionRequestData, DeletePropertyAnnualSubmissionRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def deletePropertyAnnualSubmission(request: DeletePropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    request match {
      case def1: Def1_DeletePropertyAnnualSubmissionRequestData =>
        import def1._
        val (downstreamUri, queryParams) =
          if (taxYear.useTaxYearSpecificApi) {
            (
              TaxYearSpecificIfsUri[Unit](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"),
              Nil
            )
          } else {
            (
              IfsUri[Unit](s"income-tax/business/property/annual"),
              List(
                "taxableEntityId" -> nino.nino,
                "incomeSourceId"  -> businessId.businessId,
                "taxYear"         -> taxYear.asMtd // Note that MTD tax year format is used
              )
            )
          }
        delete(downstreamUri, queryParams)
    }

  }

}
