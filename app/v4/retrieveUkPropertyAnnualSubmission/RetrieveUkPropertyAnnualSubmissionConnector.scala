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

package v4.retrieveUkPropertyAnnualSubmission

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.retrieveUkPropertyAnnualSubmission.model.request.*
import v4.retrieveUkPropertyAnnualSubmission.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v4.retrieveUkPropertyAnnualSubmission.model.{NonUkResult, Result, UkResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkPropertyAnnualSubmissionConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveUkProperty(request: RetrieveUkPropertyAnnualSubmissionRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    request match {
      case def1: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =>
        import def1.*

        val (downstreamUri, queryParams) = if (taxYear.useTaxYearSpecificApi) {
          (
            IfsUri[Def1_RetrieveUkPropertyAnnualSubmissionResponse](
              s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"),
            Nil
          )
        } else {
          (
            IfsUri[Def1_RetrieveUkPropertyAnnualSubmissionResponse](s"income-tax/business/property/annual"),
            List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear.asMtd)
          )
        }

        val response = get(downstreamUri, queryParams)

        response.map {
          case Right(ResponseWrapper(corId, resp)) if ukResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
          case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
          case Left(e)                                               => Left(e)
        }
    }

  }

  // The same API#1598 IF endpoint is used for both uk and foreign properties.
  // If a businessId of the right type is specified some of these optional fields will be present...
  private def ukResult(response: Def1_RetrieveUkPropertyAnnualSubmissionResponse): Boolean =
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty

}
