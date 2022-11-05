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
import v2.connectors.DownstreamUri.{ IfsUri, TaxYearSpecificIfsUri }
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector._
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequest
import v2.models.response.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryResponse

import scala.concurrent.{ ExecutionContext, Future }

object RetrieveUkPropertyPeriodSummaryConnector {

  sealed trait Result

  case class UkResult(response: RetrieveUkPropertyPeriodSummaryResponse) extends Result

  case object NonUkResult extends Result
}

@Singleton
class RetrieveUkPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkProperty(request: RetrieveUkPropertyPeriodSummaryRequest)(implicit hc: HeaderCarrier,
                                                                          ec: ExecutionContext,
                                                                          correlationId: String): Future[DownstreamOutcome[Result]] = {

    // Note that MTD tax year format is used

    val requestUri = if (request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveUkPropertyPeriodSummaryResponse](
        s"income-tax/business/property/${request.taxYear.asTysDownstream}/${request.nino.nino}/${request.businessId}/periodic/${request.submissionId}")
    } else {
      IfsUri[RetrieveUkPropertyPeriodSummaryResponse](s"income-tax/business/property/periodic?" +
        s"taxableEntityId=${request.nino.nino}&taxYear=${request.taxYear.asMtd}&incomeSourceId=${request.businessId}&submissionId=${request.submissionId}")
    }

    val response = get(requestUri)

    response.map {
      case Right(ResponseWrapper(corId, resp)) if ukResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
      case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
      case Left(e)                                               => Left(e)
    }
  }
  private def ukResult(response: RetrieveUkPropertyPeriodSummaryResponse): Boolean =
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty
}
