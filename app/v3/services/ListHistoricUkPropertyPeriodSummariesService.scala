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

import api.controllers.RequestContext
import api.models.domain.HistoricPropertyType
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v3.connectors.ListHistoricUkPropertyPeriodSummariesConnector
import v3.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData
import v3.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListHistoricUkPropertyPeriodSummariesService @Inject() (connector: ListHistoricUkPropertyPeriodSummariesConnector) extends BaseService {

  def listPeriodSummaries(request: ListHistoricUkPropertyPeriodSummariesRequestData, propertyType: HistoricPropertyType)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = {

    connector.listPeriodSummaries(request, propertyType).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"           -> NinoFormatError,
    "INVALID_CORRELATIONID"  -> InternalError,
    "TAX_YEAR_NOT_SUPPORTED" -> InternalError,
    "INVALID_TYPE"           -> InternalError,
    "SERVER_ERROR"           -> InternalError,
    "SERVICE_UNAVAILABLE"    -> InternalError
  )

}
