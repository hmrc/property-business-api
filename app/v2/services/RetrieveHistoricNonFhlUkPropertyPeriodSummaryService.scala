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

package v2.services

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v2.connectors.RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricNonFhlUkPropertyPeriodSummaryService @Inject() (connector: RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector)
    extends BaseService {

  def retrieve(request: RetrieveHistoricNonFhlUkPiePeriodSummaryRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]] = {

    connector.retrieve(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"        -> NinoFormatError,
      "INVALID_TYPE"        -> InternalError,
      "INVALID_DATE_FROM"   -> PeriodIdFormatError,
      "INVALID_DATE_TO"     -> PeriodIdFormatError,
      "NOT_FOUND_PROPERTY"  -> NotFoundError,
      "NOT_FOUND_PERIOD"    -> NotFoundError,
      "SERVER_ERROR"        -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

}
