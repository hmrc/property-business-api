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

import cats.implicits._
import cats.data.EitherT

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.controllers.EndpointLogContext
import v2.connectors.CreateHistoricFhlUkPiePeriodSummaryConnector
import v2.models.errors._
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRequest
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryResponse
import v2.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryService @Inject()(connector: CreateHistoricFhlUkPiePeriodSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createPeriodSummary(request: CreateHistoricFhlUkPiePeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[CreateHistoricFhlUkPiePeriodSummaryResponse]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.createPeriodSummary(request))
        .leftMap(mapDownstreamErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"            -> NinoFormatError,
      "INVALID_TYPE"            -> InternalError,
      "INVALID_PAYLOAD"         -> InternalError,
      "INVALID_CORRELATIONID"   -> InternalError,
      "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
      "DUPLICATE_SUBMISSION"    -> RuleDuplicateSubmissionError,
      "NOT_ALIGN_PERIOD"        -> RuleMisalignedPeriodError,
      "OVERLAPS_IN_PERIOD"      -> RuleOverlappingPeriodError,
      "NOT_CONTIGUOUS_PERIOD"   -> RuleNotContiguousPeriodError,
      "INVALID_PERIOD"          -> RuleToDateBeforeFromDateError,
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

}
