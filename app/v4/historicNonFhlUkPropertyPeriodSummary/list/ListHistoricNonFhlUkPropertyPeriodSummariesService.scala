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

package v4.historicNonFhlUkPropertyPeriodSummary.list

import cats.implicits.toBifunctorOps
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.request.ListHistoricNonFhlUkPropertyPeriodSummariesRequestData
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.response.ListHistoricNonFhlUkPropertyPeriodSummariesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListHistoricNonFhlUkPropertyPeriodSummariesService @Inject() (connector: ListHistoricNonFhlUkPropertyPeriodSummariesConnector)
    extends BaseService {

  def listPeriodSummaries(request: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[ListHistoricNonFhlUkPropertyPeriodSummariesResponse]] = {

    connector
      .listPeriodSummaries(request)
      .map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"           -> NinoFormatError,
      "INVALID_CORRELATIONID"  -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> InternalError,
      "INVALID_TYPE"           -> InternalError,
      "SERVER_ERROR"           -> InternalError,
      "SERVICE_UNAVAILABLE"    -> InternalError
    )

}
