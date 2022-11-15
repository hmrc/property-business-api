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

package v1.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.ListForeignPropertiesPeriodSummariesConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRequest
import v1.support.IfsResponseMappingSupport

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ListForeignPropertiesPeriodSummariesService @Inject()(connector: ListForeignPropertiesPeriodSummariesConnector)
    extends IfsResponseMappingSupport
    with Logging {

  def listForeignProperties(request: ListForeignPropertiesPeriodSummariesRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ListForeignPropertiesPeriodSummariesServiceOutcome] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.listForeignProperties(request)).leftMap(mapIfsErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_FROM_DATE"         -> DownstreamError,
      "INVALID_TO_DATE"           -> DownstreamError,
      "INVALID_DATE_REQUEST"      -> DownstreamError,
      "INVALID_CORRELATIONID"     -> DownstreamError,
      "SERVER_ERROR"              -> DownstreamError,
      "SERVICE_UNAVAILABLE"       -> DownstreamError
    )
}
