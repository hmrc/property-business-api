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

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.outcomes.ResponseWrapper
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult, Result}
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object RetrieveForeignPropertyAnnualSubmissionConnector {

  sealed trait Result

  case class ForeignResult(response: RetrieveForeignPropertyAnnualSubmissionResponse) extends Result

  case object NonForeignResult extends Result
}

@Singleton
class RetrieveForeignPropertyAnnualSubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveForeignProperty(request: RetrieveForeignPropertyAnnualSubmissionRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Result]] = {

    import request._

    val (downstreamUri, queryParams) = if (taxYear.useTaxYearSpecificApi) {
      (
        TaxYearSpecificIfsUri[RetrieveForeignPropertyAnnualSubmissionResponse](
          s"income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"),
        Nil
      )
    } else {
      // Note that MTD tax year format is used pre-TYS
      (
        IfsUri[RetrieveForeignPropertyAnnualSubmissionResponse]("income-tax/business/property/annual"),
        Seq("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId, "taxYear" -> taxYear.asMtd)
      )
    }

    val response = get(downstreamUri, queryParams)

    response.map {
      case Right(ResponseWrapper(corId, resp)) if foreignResult(resp) => Right(ResponseWrapper(corId, ForeignResult(resp)))
      case Right(ResponseWrapper(corId, _))                           => Right(ResponseWrapper(corId, NonForeignResult))
      case Left(e)                                                    => Left(e)
    }
  }

  // The same API#1598 IF endpoint is used for both uk and foreign properties.
  // If a businessId of the right type is specified some of these optional fields will be present...
  private def foreignResult(response: RetrieveForeignPropertyAnnualSubmissionResponse): Boolean =
    response.foreignFhlEea.nonEmpty || response.foreignNonFhlProperty.nonEmpty

}
