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
import v2.connectors.AmendHistoricNonFhlUkPiePeriodSummaryConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryRequest
import v2.services.AmendHistoricNonFhlUkPiePeriodSummaryService.downstreamErrorMap
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendHistoricNonFhlUkPiePeriodSummaryService @Inject()(connector: AmendHistoricNonFhlUkPiePeriodSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def amend(request: AmendHistoricNonFhlUkPiePeriodSummaryRequest)(implicit hc: HeaderCarrier,
                                                                   ec: ExecutionContext,
                                                                   logContext: EndpointLogContext,
                                                                   correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = EitherT(connector.amend(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))

    result.value
  }

}

object AmendHistoricNonFhlUkPiePeriodSummaryService {

  val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"                -> NinoFormatError,
      "INVALID_TYPE"                -> InternalError,
      "INVALID_PAYLOAD"             -> InternalError,
      "INVALID_DATE_FROM"           -> PeriodIdFormatError,
      "INVALID_DATE_TO"             -> PeriodIdFormatError,
      "INVALID_CORRELATIONID"       -> InternalError,
      "SUBMISSION_PERIOD_NOT_FOUND" -> NotFoundError,
      "NOT_FOUND_PROPERTY"          -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE"     -> NotFoundError,
      "NOT_FOUND"                   -> NotFoundError,
      "BOTH_EXPENSES_SUPPLIED"      -> RuleBothExpensesSuppliedError,
      "SERVER_ERROR"                -> InternalError,
      "SERVICE_UNAVAILABLE"         -> InternalError
    )
}
