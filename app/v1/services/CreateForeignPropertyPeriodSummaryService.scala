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

package v1.services

import cats.implicits._
import cats.data.EitherT
import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateForeignPropertyPeriodSummaryConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequest
import v1.support.IfsResponseMappingSupport

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateForeignPropertyPeriodSummaryService @Inject()(connector: CreateForeignPropertyPeriodSummaryConnector)
    extends IfsResponseMappingSupport
    with Logging {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[CreateForeignPropertyPeriodSummaryServiceOutcome] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.createForeignProperty(request)).leftMap(mapIfsErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
      "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
      "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
      "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
      "SERVER_ERROR"              -> DownstreamError,
      "SERVICE_UNAVAILABLE"       -> DownstreamError,
      "INVALID_PAYLOAD"           -> DownstreamError,
      "INVALID_CORRELATIONID"     -> DownstreamError,
      "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmission,
      "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError
    )

}
