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

package v4.historicNonFhlUkPropertyPeriodSummary.create

import cats.implicits.toBifunctorOps
import common.models.domain.PeriodId
import common.models.errors._
import shared.controllers.RequestContext
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{BaseService, ServiceOutcome}
import v4.historicNonFhlUkPropertyPeriodSummary.create.model.request.CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData
import v4.historicNonFhlUkPropertyPeriodSummary.create.model.response.CreateHistoricNonFhlUkPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricNonFhlUkPropertyPeriodSummaryService @Inject() (connector: CreateHistoricNonFhlUkPropertyPeriodSummaryConnector)
    extends BaseService {

  def createPeriodSummary(request: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[CreateHistoricNonFhlUkPropertyPeriodSummaryResponse]] = {

    def toResponse(wrapper: ResponseWrapper[Unit]): ResponseWrapper[CreateHistoricNonFhlUkPropertyPeriodSummaryResponse] =
      wrapper
        .map(_ => CreateHistoricNonFhlUkPropertyPeriodSummaryResponse(PeriodId(request.body.fromDate, request.body.toDate)))

    connector.createPeriodSummary(request).map(_.map(toResponse).leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
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
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

}
