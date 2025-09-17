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

package v4.createUkPropertyPeriodSummary

import cats.implicits.*
import common.models.errors.*
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v4.createUkPropertyPeriodSummary.model.request.CreateUkPropertyPeriodSummaryRequestData
import v4.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateUkPropertyPeriodSummaryService @Inject() (connector: CreateUkPropertyPeriodSummaryConnector) extends BaseService {

  def createUkProperty(request: CreateUkPropertyPeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[CreateUkPropertyPeriodSummaryResponse]] = {

    connector.createUkProperty(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
      "INVALID_PAYLOAD"           -> InternalError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
      "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmissionError,
      "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
      "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
      "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
      "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError,
      "MISSING_EXPENSES"          -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID"            -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"             -> InternalError,
      "PERIOD_NOT_ALIGNED"                 -> RuleMisalignedPeriodError,
      "PERIOD_OVERLAPS"                    -> RuleOverlappingPeriodError,
      "SUBMISSION_DATE_ISSUE"              -> RuleMisalignedPeriodError,
      "BUSINESS_INCOME_PERIOD_RESTRICTION" -> InternalError
      //      "INVALID_SUBMISSION_PERIOD"   -> RuleInvalidSubmissionPeriodError,
      //      "INVALID_SUBMISSION_END_DATE" -> RuleInvalidSubmissionEndDateError
      //      To be reinstated, see MTDSA-15575
    )

    errors ++ extraTysErrors
  }

}
