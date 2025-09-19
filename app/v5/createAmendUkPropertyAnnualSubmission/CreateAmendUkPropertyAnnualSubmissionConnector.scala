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

package v5.createAmendUkPropertyAnnualSubmission

import play.api.http.Status.NO_CONTENT
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.domain.TaxYear
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v5.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def createAmendUkPropertyAnnualSubmission(request: CreateAmendUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    import request.*

    val downstreamUri = taxYear match {
      case ty if ty.year >= TaxYear.tysTaxYear.year =>
        IfsUri[Unit](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId")
      case _ =>
        IfsUri[Unit](s"income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=${taxYear.asMtd}")
    }

    put(body, downstreamUri)
  }

}
