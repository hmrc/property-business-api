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

package v6.retrieveForeignPropertyPeriodSummary

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v6.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult, Result}
import v6.retrieveForeignPropertyPeriodSummary.model.request._
import v6.retrieveForeignPropertyPeriodSummary.model.response._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveForeignPropertyPeriodSummaryConnector {

  sealed trait Result

  case class ForeignResult(response: RetrieveForeignPropertyPeriodSummaryResponse) extends Result

  case object NonForeignResult extends Result
}

@Singleton
class RetrieveForeignPropertyPeriodSummaryConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveForeignProperty(request: RetrieveForeignPropertyPeriodSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    request match {
      case def1: Def1_RetrieveForeignPropertyPeriodSummaryRequestData =>
        import def1._
        val (downstreamUri, queryParams) =
          if (taxYear.useTaxYearSpecificApi) {
            (
              TaxYearSpecificIfsUri[Def1_RetrieveForeignPropertyPeriodSummaryResponse](
                s"income-tax/business/property/${taxYear.asTysDownstream}/$nino/$businessId/periodic/$submissionId"),
              Nil
            )
          } else {
            (
              IfsUri[Def1_RetrieveForeignPropertyPeriodSummaryResponse]("income-tax/business/property/periodic"),
              List(
                "taxableEntityId" -> nino.nino,
                "taxYear"         -> taxYear.asMtd, // Note that MTD tax year format is used
                "incomeSourceId"  -> businessId.businessId,
                "submissionId"    -> submissionId.submissionId
              )
            )
          }

        val response = get(downstreamUri, queryParams)

        response.map(_.map {
          case ResponseWrapper(corId, resp) if foreignResult(resp) => ResponseWrapper(corId, ForeignResult(resp))
          case ResponseWrapper(corId, _)                           => ResponseWrapper(corId, NonForeignResult)
        })

    }
  }

  // The same API#1595 IF endpoint is used for both uk and foreign properties.
  // If a businessId of the right type is specified some of these optional fields will be present...
  private def foreignResult(response: RetrieveForeignPropertyPeriodSummaryResponse): Boolean =
    response match {
      case def1: Def1_RetrieveForeignPropertyPeriodSummaryResponse =>
        def1.foreignFhlEea.nonEmpty || def1.foreignNonFhlProperty.nonEmpty
    }

}
