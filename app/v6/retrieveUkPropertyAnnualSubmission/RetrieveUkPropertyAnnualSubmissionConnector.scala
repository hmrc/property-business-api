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

package v6.retrieveUkPropertyAnnualSubmission

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.model.{NonUkResult, Result, UkResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveUkProperty(request: RetrieveUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {
    import request.*
    import schema.*

    lazy val downstreamUri1805: (DownstreamUri[DownstreamResp], Seq[(String, String)]) =
      if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1805") && taxYear.year >= 2026) {
        (HipUri[DownstreamResp](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"), Nil)
      } else {
        (IfsUri[DownstreamResp](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"), Nil)
      }

    lazy val downstreamUri1598: (DownstreamUri[DownstreamResp], Seq[(String, String)]) = (
      IfsUri[DownstreamResp]("income-tax/business/property/annual"),
      List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear.asMtd)
    )

    val (downstreamUri, queryParams) = if (taxYear.useTaxYearSpecificApi) downstreamUri1805 else downstreamUri1598

    val response = get(downstreamUri, queryParams)

    response.map {
      case Right(ResponseWrapper(corId, resp)) if resp.hasUkData => Right(ResponseWrapper(corId, UkResult(resp)))
      case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
      case Left(e)                                               => Left(e)
    }

  }

}
