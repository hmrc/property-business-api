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

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.CreateHistoricNonFhlUkPropertyPeriodSummaryConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.CreateHistoricNonFhlUkPropertyPeriodSummaryRequest
import v2.models.response.createHistoricNonFhlUkPiePeriodSummary.CreateHistoricNonFhlUkPiePeriodSummaryResponse
import v2.support.DownstreamResponseMappingSupport

@Singleton
class CreateHistoricNonFhlUkPropertyPeriodSummaryService @Inject()(connector: CreateHistoricNonFhlUkPropertyPeriodSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createHistoricNonFhlUkProperty(request: CreateHistoricNonFhlUkPropertyPeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[CreateHistoricNonFhlUkPiePeriodSummaryResponse]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.createPeriodSummary(request)).leftMap(mapDownstreamErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
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
      "BOTH_EXPENSES_SUPPLIED"  -> RuleBothExpensesSuppliedError,
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )
}
