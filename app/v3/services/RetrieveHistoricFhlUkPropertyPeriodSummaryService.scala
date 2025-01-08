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

package v3.services

import cats.implicits._
import common.models.errors.PeriodIdFormatError
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v3.connectors.RetrieveHistoricFhlUkPropertyPeriodSummaryConnector
import v3.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequestData
import v3.models.response.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricFhlUkPropertyPeriodSummaryService @Inject() (connector: RetrieveHistoricFhlUkPropertyPeriodSummaryConnector)
    extends BaseService {

  def retrieve(request: RetrieveHistoricFhlUkPiePeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext
  ): Future[ServiceOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse]] = {

    connector.retrieve(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"        -> NinoFormatError,
      "INVALID_DATE_FROM"   -> PeriodIdFormatError,
      "INVALID_DATE_TO"     -> PeriodIdFormatError,
      "INVALID_TYPE"        -> InternalError,
      "NOT_FOUND_PROPERTY"  -> NotFoundError,
      "NOT_FOUND_PERIOD"    -> NotFoundError,
      "SERVER_ERROR"        -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

}
