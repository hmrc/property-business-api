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

package v4.retrieveUkPropertyPeriodSummary

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryConnector._
import v4.retrieveUkPropertyPeriodSummary.model.request._
import v4.retrieveUkPropertyPeriodSummary.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveUkPropertyPeriodSummaryConnector {

  sealed trait Result

  case class UkResult(response: RetrieveUkPropertyPeriodSummaryResponse) extends Result

  case object NonUkResult extends Result
}

@Singleton
class RetrieveUkPropertyPeriodSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveUkPropertyPeriodSummary(request: RetrieveUkPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    request match {
      case def1: Def1_RetrieveUkPropertyPeriodSummaryRequestData =>
        import def1._

        val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
          TaxYearSpecificIfsUri[Def1_RetrieveUkPropertyPeriodSummaryResponse](
            s"income-tax/business/property/${taxYear.asTysDownstream}/$nino/$businessId/periodic/$submissionId")
        } else {
          IfsUri[Def1_RetrieveUkPropertyPeriodSummaryResponse](
            s"income-tax/business/property/periodic?" +
              s"taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId")
        }
        val response = get(downstreamUri)

        response.map {
          case Right(ResponseWrapper(corId, resp)) if ukResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
          case Right(ResponseWrapper(corId, _))                      => Right(ResponseWrapper(corId, NonUkResult))
          case Left(e)                                               => Left(e)
        }

      case def2: Def2_RetrieveUkPropertyPeriodSummaryRequestData =>
        import def2._

        val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
          TaxYearSpecificIfsUri[Def2_RetrieveUkPropertyPeriodSummaryResponse](
            s"income-tax/business/property/${taxYear.asTysDownstream}/$nino/$businessId/periodic/$submissionId")
        } else {
          IfsUri[Def2_RetrieveUkPropertyPeriodSummaryResponse](
            s"income-tax/business/property/periodic?" +
              s"taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId")
        }
        val response = get(downstreamUri)

        response.map {
          case Right(ResponseWrapper(corId, resp)) if def2UkResult(resp) => Right(ResponseWrapper(corId, UkResult(resp)))
          case Right(ResponseWrapper(corId, _))                          => Right(ResponseWrapper(corId, NonUkResult))
          case Left(e)                                                   => Left(e)
        }
    }
  }

  private def ukResult(response: Def1_RetrieveUkPropertyPeriodSummaryResponse): Boolean = {
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty
  }

  private def def2UkResult(response: Def2_RetrieveUkPropertyPeriodSummaryResponse): Boolean = {
    response.ukFhlProperty.nonEmpty || response.ukNonFhlProperty.nonEmpty
  }

}
