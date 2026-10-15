/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.http.Status.*

// MtdError types that are common across MTD APIs.

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The NINO format is invalid", BAD_REQUEST)

object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The taxYear format is invalid", BAD_REQUEST)

object ToDateFormatError extends MtdError(code = "FORMAT_TO_DATE", message = "The toDate format is invalid", BAD_REQUEST)

object FromDateFormatError extends MtdError(code = "FORMAT_FROM_DATE", message = "The fromDate format is invalid", BAD_REQUEST)

object StartDateFormatError extends MtdError("FORMAT_START_DATE", "The provided start date is invalid", BAD_REQUEST)

object EndDateFormatError extends MtdError("FORMAT_END_DATE", "The provided end date is invalid", BAD_REQUEST)

object DateFormatError extends MtdError(code = "FORMAT_DATE", message = "The supplied date format is not valid", BAD_REQUEST)

object BusinessIdFormatError extends MtdError("FORMAT_BUSINESS_ID", "The Business ID format is invalid", BAD_REQUEST)

object CountryCodeFormatError extends MtdError("FORMAT_COUNTRY_CODE", "The provided Country code is invalid", BAD_REQUEST)

object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided Submission ID is invalid", BAD_REQUEST)

object PropertyIdFormatError extends MtdError("FORMAT_PROPERTY_ID", "The provided property ID is invalid", BAD_REQUEST)

object PropertyNameFormatError extends MtdError("FORMAT_PROPERTY_NAME", "The provided property name is invalid", BAD_REQUEST)

object EndReasonFormatError extends MtdError("FORMAT_END_REASON", "The provided end reason is invalid", BAD_REQUEST)

object PeriodIdFormatError extends MtdError("FORMAT_PERIOD_ID", "The provided period id is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object StringFormatError extends MtdError(code = "FORMAT_STRING", message = "The supplied string format is not valid", BAD_REQUEST)
//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)

object GatewayTimeoutError extends MtdError("GATEWAY_TIMEOUT", "The request has timed out", GATEWAY_TIMEOUT)

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)

object InvalidHttpMethodError extends MtdError("INVALID_HTTP_METHOD", "Invalid HTTP method", METHOD_NOT_ALLOWED)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Authentication/Authorisation errors

/** Authentication OK but not allowed access to the requested resource
  */
object ClientOrAgentNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client or agent is not authorised", FORBIDDEN) {
  def withStatus401: MtdError = copy(httpStatus = UNAUTHORIZED)
}

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)

// Common rule errors

object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "A tax year range of one year is required", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RuleDateRangeInvalidError extends MtdError(code = "RULE_DATE_RANGE_INVALID", message = "The specified date range is invalid", BAD_REQUEST)

object RuleEndBeforeStartDateError
    extends MtdError("RULE_END_DATE_BEFORE_START_DATE", "The supplied accounting period end date is before the start date", BAD_REQUEST)

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a permitted ISO 3166-1 alpha-3 country code", BAD_REQUEST)

object RuleDuplicatePropertyNameError
    extends MtdError("RULE_DUPLICATE_PROPERTY_NAME", "Foreign property name already exists for the same country", BAD_REQUEST)

object RuleTaxYearBeforeBusinessStartError
    extends MtdError(
      "RULE_TAX_YEAR_BEFORE_BUSINESS_START",
      "Foreign property cannot be reported for a tax year before the first tax year of the business",
      BAD_REQUEST)

object RuleEndDateBeforeTaxYearStartError
    extends MtdError("RULE_END_DATE_BEFORE_TAX_YEAR_START", "The end date is before the start of the tax year", BAD_REQUEST)

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

object RuleBothExpensesSuppliedError
    extends MtdError("RULE_BOTH_EXPENSES_SUPPLIED", "Both Expenses and Consolidated Expenses must not be present at the same time", BAD_REQUEST)

object RuleBothExpensesSuppliedWithForeignPropertyError
    extends MtdError(
      "RULE_BOTH_EXPENSES_SUPPLIED",
      "Only one of either expenses or consolidatedExpenses can be supplied. A single expense type must be used for all foreign properties",
      BAD_REQUEST)

object RuleToDateBeforeFromDateError
    extends MtdError("RULE_TO_DATE_BEFORE_FROM_DATE", "The To date cannot be earlier than the From date", BAD_REQUEST)

object RuleDuplicateSubmissionError
    extends MtdError("RULE_DUPLICATE_SUBMISSION", "A summary has already been submitted for the period specified", BAD_REQUEST)

object RuleNotContiguousPeriodError extends MtdError("RULE_NOT_CONTIGUOUS_PERIOD", "Period summaries are not contiguous", BAD_REQUEST)

object RuleMisalignedPeriodError extends MtdError("RULE_MISALIGNED_PERIOD", "Period summary is not within the accounting period", BAD_REQUEST)

object RuleOverlappingPeriodError
    extends MtdError("RULE_OVERLAPPING_PERIOD", "Period summary overlaps with any of the existing period summaries", BAD_REQUEST)

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

object RuleDuplicatePropertyIdError
    extends MtdError("RULE_DUPLICATE_PROPERTY_ID", "You cannot supply the same property ID for multiple properties", BAD_REQUEST) {

  def forDuplicatedIdsAndPaths(id: String, paths: Seq[String]): MtdError = copy(
    message = s"The property ID '$id' is duplicated for multiple properties",
    paths = Some(paths)
  )

}

//Stub Errors
object RuleIncorrectGovTestScenarioError
    extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The supplied Gov-Test-Scenario is not valid", BAD_REQUEST)
