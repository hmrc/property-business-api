/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.retrieveForeignPropertyAnnualSubmission

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.model.{ForeignResult, NonForeignResult, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieveForeignProperty(request: RetrieveForeignPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {
    import request.*
    import schema.*

    lazy val downstreamUriForTy2627Onwards: DownstreamUri[DownstreamResp] =
      HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/foreign-property/annual/$nino/$businessId")

    lazy val downstreamUri1805: DownstreamUri[DownstreamResp] =
      if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1805")) {
        HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId")
      } else {
        IfsUri[DownstreamResp](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId")
      }

    lazy val downstreamUri1598: DownstreamUri[DownstreamResp] = IfsUri[DownstreamResp]("income-tax/business/property/annual")

    val queryParamsTy2627Onwards: Seq[(String, String)] = propertyId.toSeq.map(pid => "propertyId" -> pid.propertyId)

    val queryParams1598: Seq[(String, String)] = List(
      "taxableEntityId" -> nino.nino,
      "incomeSourceId"  -> businessId.businessId,
      "taxYear"         -> taxYear.asMtd
    )

    val (downstreamUri, queryParams): (DownstreamUri[DownstreamResp], Seq[(String, String)]) = taxYear match {
      case ty if ty.year >= 2027          => downstreamUriForTy2627Onwards -> queryParamsTy2627Onwards
      case ty if ty.useTaxYearSpecificApi => downstreamUri1805             -> Nil
      case _                              => downstreamUri1598             -> queryParams1598
    }

    get(downstreamUri, queryParams).map {
      case Right(ResponseWrapper(corId, resp)) if resp.hasForeignData => Right(ResponseWrapper(corId, ForeignResult(resp)))
      case Right(ResponseWrapper(corId, _))                           => Right(ResponseWrapper(corId, NonForeignResult))
      case Left(e)                                                    => Left(e)
    }

  }

}
