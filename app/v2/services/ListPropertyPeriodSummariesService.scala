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
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.ListPropertyPeriodSummariesConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequest
import v2.models.response.listPropertyPeriodSummaries.ListPropertyPeriodSummariesResponse
import v2.support.IfsResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPropertyPeriodSummariesService @Inject()(connector: ListPropertyPeriodSummariesConnector)
  extends IfsResponseMappingSupport with Logging {

  def listPeriodSummaries(request: ListPropertyPeriodSummariesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[ServiceOutcome[ListPropertyPeriodSummariesResponse]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.listPeriodSummaries(request)).leftMap(mapIfsErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "NO_DATA_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
