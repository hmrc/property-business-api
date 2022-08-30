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

package v2.services

import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.ListHistoricUkPropertyPeriodSummariesConnector
import v2.controllers.EndpointLogContext
import v2.models.domain.HistoricPropertyType
import v2.models.errors._
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequest
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod }
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ListHistoricUkPropertyPeriodSummariesService @Inject()(connector: ListHistoricUkPropertyPeriodSummariesConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def listPeriodSummaries(request: ListHistoricUkPropertyPeriodSummariesRequest, propertyType: HistoricPropertyType)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.listPeriodSummaries(request, propertyType)).leftMap(mapDownstreamErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap = Map(
    "INVALID_NINO"           -> NinoFormatError,
    "INVALID_CORRELATIONID"  -> InternalError,
    "TAX_YEAR_NOT_SUPPORTED" -> InternalError,
    "SERVER_ERROR"           -> InternalError,
    "SERVICE_UNAVAILABLE"    -> InternalError
  )
}
