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

package v6.createAmendForeignPropertyAnnualSubmission

import play.api.http.Status.NO_CONTENT
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def createAmendForeignPropertyAnnualSubmission(request: CreateAmendForeignPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request.*

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    lazy val downstreamUri1597 =
      IfsUri[Unit](s"income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=${taxYear.asMtd}")

    lazy val downstreamUri1804 = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1804") && taxYear.year == 2026) {
      HipUri[Unit](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId")
    } else {
      IfsUri[Unit](s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId")
    }

    lazy val downstreamUriForTy2627Onwards =
      HipUri[Unit](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/business/foreign-property/annual/$nino/$businessId")

    val downstreamUri = taxYear match {
      case ty if ty.year >= 2027          => downstreamUriForTy2627Onwards
      case ty if ty.useTaxYearSpecificApi => downstreamUri1804
      case _                              => downstreamUri1597
    }

    put(body, downstreamUri)
  }

}
