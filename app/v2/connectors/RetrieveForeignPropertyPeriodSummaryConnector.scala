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
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector._
import v2.connectors.httpparsers.StandardHttpParser._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
import v2.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse

import scala.concurrent.{ ExecutionContext, Future }

object RetrieveForeignPropertyPeriodSummaryConnector {

  sealed trait Result

  case class ForeignResult(response: RetrieveForeignPropertyPeriodSummaryResponse) extends Result

  case object NonForeignResult extends Result
}

@Singleton
class RetrieveForeignPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveForeignProperty(request: RetrieveForeignPropertyPeriodSummaryRequest)(implicit hc: HeaderCarrier,
                                                                                    ec: ExecutionContext,
                                                                                    correlationId: String): Future[DownstreamOutcome[Result]] = {
    val response = get(
      uri = IfsUri[RetrieveForeignPropertyPeriodSummaryResponse]("income-tax/business/property/periodic"),
      queryParams = Seq("taxableEntityId" -> request.nino.value,
                        "taxYear"         -> request.taxYear,
                        "incomeSourceId"  -> request.businessId,
                        "submissionId"    -> request.submissionId)
    )

    response.map {
      case Right(ResponseWrapper(corId, resp)) if foreignResult(resp) => Right(ResponseWrapper(corId, ForeignResult(resp)))
      case Right(ResponseWrapper(corId, _))                           => Right(ResponseWrapper(corId, NonForeignResult))
      case Left(e)                                                    => Left(e)
    }
  }

  //The same API#1595 IF endpoint is used for both uk and foreign properties.
  //If a businessId of the right type is specified some of these optional fields will be present...
  private def foreignResult(response: RetrieveForeignPropertyPeriodSummaryResponse): Boolean =
    response.foreignFhlEea.nonEmpty || response.foreignNonFhlProperty.nonEmpty
}
