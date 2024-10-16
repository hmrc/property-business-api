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

package v5.createAmendUkPropertyCumulativeSummary

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v5.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData
import v5.createAmendUkPropertyCumulativeSummary.model.response.CreateAmendUkPropertyCumulativeSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkPropertyCumulativeSummaryService @Inject() (connector: CreateAmendUkPropertyCumulativeSummaryConnector) extends BaseService {

  def createAmendUkPropertyCumulativeSummary(request: CreateAmendUkPropertyCumulativeSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[CreateAmendUkPropertyCumulativeSummaryResponse]] = {

    connector.createAmendUkPropertyCumulativeSummary(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"          -> NinoFormatError,
      "INVALID_INCOME_SOURCE_ID"           -> BusinessIdFormatError,
      "INVALID_PAYLOAD"                    -> InternalError,
      "INVALID_CORRELATION_ID"             -> InternalError,
      "INVALID_TAX_YEAR"                   -> TaxYearFormatError,
      "INCOME_SOURCE_NOT_FOUND"            -> NotFoundError,
      "INCOME_SOURCE_DATA_NOT_FOUND"       -> NotFoundError,
      "MISSING_EXPENSES"                   -> InternalError,
      "INVALID_SUBMISSION_END_DATE"        -> RuleInvalidSubmissionEndDateError,
      "SUBMISSION_END_DATE_VALUE"          -> RuleSubmissionEndDateError,
      "INVALID_START_DATE"                 -> RuleStartDateNotAlignedWithReportingType,
      "START_DATE_NOT_ALIGNED"             -> RuleStartDateNotAlignedToCommencementDate,
      "END_DATE_NOT_ALIGNED"               -> RuleEndDateNotAlignedWithReportingType,
      "MISSING_SUBMISSION_DATES"           -> RuleMissingSubmissionDates,
      "START_END_DATE_NOT_ACCEPTED"        -> RuleStartAndEndDateNotAllowed,
      "OUTSIDE_AMENDMENT_WINDOW"           -> RuleOutsideAmendmentWindow,
      "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError,
      "EARLY_DATA_SUBMISSION_NOT_ACCEPTED" -> RuleEarlyDataSubmissionNotAccepted,
      "DUPLICATE_COUNTRY_CODE"             -> RuleDuplicateCountryCode,
      "SERVER_ERROR"                       -> InternalError,
      "SERVICE_UNAVAILABLE"                -> InternalError
    )

    errors
  }

}
