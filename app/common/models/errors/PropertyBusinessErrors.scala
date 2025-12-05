/*
 * Copyright 2025 HM Revenue & Customs
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

package common.models.errors

import play.api.http.Status.BAD_REQUEST
import shared.models.errors.MtdError

object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided Submission ID is invalid", BAD_REQUEST)

object PropertyIdFormatError extends MtdError("FORMAT_PROPERTY_ID", "The provided property ID is invalid", BAD_REQUEST)

object PropertyNameFormatError extends MtdError("FORMAT_PROPERTY_NAME", "The provided property name is invalid", BAD_REQUEST)

object EndReasonFormatError extends MtdError("FORMAT_END_REASON", "The provided end reason is invalid", BAD_REQUEST)

object RuleDuplicatePropertyNameError
    extends MtdError("RULE_DUPLICATE_PROPERTY_NAME", "Foreign property name already exists for the same country", BAD_REQUEST)

object RuleTaxYearBeforeBusinessStartError
    extends MtdError(
      "RULE_TAX_YEAR_BEFORE_BUSINESS_START",
      "Foreign property cannot be reported for a tax year before the first tax year of the business",
      BAD_REQUEST)

object RuleEndDateAfterTaxYearEndError
    extends MtdError("RULE_END_DATE_AFTER_TAX_YEAR_END", "The end date is after the end of the tax year", BAD_REQUEST)

object RulePropertyBusinessCeasedError
    extends MtdError("RULE_PROPERTY_BUSINESS_CEASED", "Foreign property business income source has ceased", BAD_REQUEST)

object RuleMissingEndDetailsError extends MtdError("RULE_MISSING_END_DETAILS", "End date and end reason must be supplied together", BAD_REQUEST)

object RuleTypeOfBusinessIncorrectError
    extends MtdError("RULE_TYPE_OF_BUSINESS_INCORRECT", "The businessId is for a different type of business", BAD_REQUEST)

object RulePropertyIncomeAllowanceError
    extends MtdError(
      "RULE_PROPERTY_INCOME_ALLOWANCE",
      "The propertyIncomeAllowance cannot be submitted if privateUseAdjustment is supplied",
      BAD_REQUEST)

object RuleDuplicateCountryCodeError
    extends MtdError("RULE_DUPLICATE_COUNTRY_CODE", "You cannot supply the same country code for multiple properties", BAD_REQUEST) {

  def forDuplicatedCodesAndPaths(code: String, paths: Seq[String]): MtdError =
    RuleDuplicateCountryCodeError.copy(message = s"The country code '$code' is duplicated for multiple properties", paths = Some(paths))

}

object RuleBothAllowancesSuppliedError
    extends MtdError("RULE_BOTH_ALLOWANCES_SUPPLIED", "Both allowances and property allowances must not be present at the same time", BAD_REQUEST)

object RuleBuildingNameNumberError
    extends MtdError("RULE_BUILDING_NAME_NUMBER", "Postcode must be supplied along with at least one of name or number", BAD_REQUEST)

object RuleHistoricTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleBothExpensesSuppliedError
    extends MtdError("RULE_BOTH_EXPENSES_SUPPLIED", "Both Expenses and Consolidated Expenses must not be present at the same time", BAD_REQUEST)

object RuleToDateBeforeFromDateError
    extends MtdError("RULE_TO_DATE_BEFORE_FROM_DATE", "The To date cannot be earlier than the From date", BAD_REQUEST)

object RuleDuplicateSubmissionError
    extends MtdError("RULE_DUPLICATE_SUBMISSION", "A summary has already been submitted for the period specified", BAD_REQUEST)

object RuleNotContiguousPeriodError extends MtdError("RULE_NOT_CONTIGUOUS_PERIOD", "Period summaries are not contiguous", BAD_REQUEST)

object RuleMisalignedPeriodError extends MtdError("RULE_MISALIGNED_PERIOD", "Period summary is not within the accounting period", BAD_REQUEST)

object RuleOverlappingPeriodError
    extends MtdError("RULE_OVERLAPPING_PERIOD", "Period summary overlaps with any of the existing period summaries", BAD_REQUEST)

object PeriodIdFormatError extends MtdError("FORMAT_PERIOD_ID", "The provided period id is invalid", BAD_REQUEST)

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

object RuleMissingSubmissionDatesError
    extends MtdError(
      "RULE_MISSING_SUBMISSION_DATES",
      "Submission start/end date not provided",
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
      "You are outside the amendment window",
      BAD_REQUEST
    )

object RuleEarlyDataSubmissionNotAcceptedError
    extends MtdError(
      "RULE_EARLY_DATA_SUBMISSION_NOT_ACCEPTED",
      "Cannot submit data more than 10 days before end of period",
      BAD_REQUEST
    )

object RulePropertyIdMismatchError
    extends MtdError(
      "RULE_PROPERTY_ID_MISMATCH",
      "The supplied property ID is not valid for this income source",
      BAD_REQUEST
    )
