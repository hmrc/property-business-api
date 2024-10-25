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

package api.models.errors

import play.api.http.Status._

object RuleAdvanceSubmissionRequiresPeriodEndDateError
    extends MtdError(
      "RULE_ADVANCE_SUBMISSION_REQUIRES_PERIOD_END_DATE",
      "For advance submission, end date must be the end of the period",
      BAD_REQUEST
    )

object RuleSubmissionEndDateCannotMoveBackwardsError
    extends MtdError(
      "RULE_SUBMISSION_END_DATE_CANNOT_MOVE_BACKWARDS",
      "Submission end date cannot be earlier than existing submission",
      BAD_REQUEST
    )

object RuleStartDateNotAlignedWithReportingTypeError
    extends MtdError(
      "RULE_START_DATE_NOT_ALIGNED_WITH_REPORTING_TYPE",
      "Start date does not align with the reporting type",
      BAD_REQUEST
    )

object RuleStartDateNotAlignedToCommencementDateError
    extends MtdError(
      "RULE_START_DATE_NOT_ALIGNED_TO_COMMENCEMENT_DATE",
      "Start date does not align with the commencement date",
      BAD_REQUEST
    )

object RuleEndDateNotAlignedWithReportingTypeError
    extends MtdError(
      "RULE_END_DATE_NOT_ALIGNED_WITH_REPORTING_TYPE",
      "End date does not align with the reporting type",
      BAD_REQUEST
    )

object RuleStartAndEndDateNotAllowedError
    extends MtdError(
      "RULE_START_AND_END_DATE_NOT_ALLOWED",
      "Start/end date not accepted for annual/latent submission",
      BAD_REQUEST
    )

object RuleOutsideAmendmentWindowError
    extends MtdError(
      "RULE_OUTSIDE_AMENDMENT_WINDOW",
      "Request cannot be completed as you are outside the amendment window",
      BAD_REQUEST
    )

object RuleEarlyDataSubmissionNotAcceptedError
    extends MtdError(
      "RULE_EARLY_DATA_SUBMISSION_NOT_ACCEPTED",
      "Cannot submit data more than 10 days before end of period",
      BAD_REQUEST
    )

object RuleMissingSubmissionDatesError
    extends MtdError(
      "RULE_MISSING_SUBMISSION_DATES",
      "Submission start/end date not provided",
      BAD_REQUEST
    )

object RuleAdvanceSubmissionRequiresPeriodEndDate
    extends MtdError(
      "RULE_ADVANCE_SUBMISSION_REQUIRES_PERIOD_END_DATE",
      "For advance submission end date must be the end of the period.",
      BAD_REQUEST)

object RuleSubmissionEndDateCannotMoveBackwards
    extends MtdError("RULE_SUBMISSION_END_DATE_CANNOT_MOVE_BACKWARDS", "Submission end date cannot be earlier than existing submission.", BAD_REQUEST)

object RuleStartDateNotAlignedWithReportingType
    extends MtdError("RULE_START_DATE_NOT_ALIGNED_WITH_REPORTING_TYPE", "Start date does not align to the reporting type.", BAD_REQUEST)

object RuleStartDateNotAlignedToCommencementDate
    extends MtdError("RULE_START_DATE_NOT_ALIGNED_TO_COMMENCEMENT_DATE", "Start date does not align to the commencement date.", BAD_REQUEST)

object RuleEndDateNotAlignedWithReportingType
    extends MtdError("RULE_END_DATE_NOT_ALIGNED_WITH_REPORTING_TYPE", "End date does not align to the reporting type.", BAD_REQUEST)

object RuleMissingSubmissionDates extends MtdError("RULE_MISSING_SUBMISSION_DATES", "Submission start/end date not provided.", BAD_REQUEST)

object RuleStartAndEndDateNotAllowed
    extends MtdError("RULE_START_AND_END_DATE_NOT_ALLOWED", "Start/end date not accepted for annual/latent submission.", BAD_REQUEST)

object RuleOutsideAmendmentWindow
    extends MtdError("RULE_OUTSIDE_AMENDMENT_WINDOW", "Request cannot be completed as you are outside the amendment window.", BAD_REQUEST)

object RuleEarlyDataSubmissionNotAccepted
    extends MtdError("RULE_EARLY_DATA_SUBMISSION_NOT_ACCEPTED", "Cannot submit data more than 10 days before end of Period.", BAD_REQUEST)

object RuleDuplicateCountryCode
    extends MtdError("RULE_DUPLICATE_COUNTRY_CODE", "You cannot supply the same country code for multiple properties.", BAD_REQUEST)
