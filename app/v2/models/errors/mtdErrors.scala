/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.errors

import play.api.libs.json.{Json, OWrites}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
  implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)
}

object MtdErrorWithCustomMessage {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

object NinoFormatError         extends MtdError("FORMAT_NINO", "The provided NINO is invalid")
object BusinessIdFormatError   extends MtdError("FORMAT_BUSINESS_ID", "The provided Business ID is invalid")
object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided Submission ID is invalid")
object FromDateFormatError     extends MtdError("FORMAT_FROM_DATE", "The provided From date is invalid")
object ToDateFormatError       extends MtdError("FORMAT_TO_DATE", "The provided To date is invalid")
object DateFormatError         extends MtdError("FORMAT_DATE", "The supplied date format is not valid")
object StringFormatError       extends MtdError("FORMAT_STRING", "The supplied string format is not valid")
object CountryCodeFormatError  extends MtdError("FORMAT_COUNTRY_CODE", "The provided Country code is invalid")
object ValueFormatError        extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99")
object TaxYearFormatError      extends MtdError("FORMAT_TAX_YEAR", "The provided Tax year is invalid")

// Rule Errors
object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")
object RuleDuplicateCountryCodeError      extends MtdError("RULE_DUPLICATE_COUNTRY_CODE", "You cannot supply the same country code for multiple properties") {
  def forDuplicatedCodesAndPaths(code: String, paths: Seq[String]): MtdError =
    RuleDuplicateCountryCodeError.copy(message = s"The country code '$code' is duplicated for multiple properties", paths = Some(paths))
}

object RuleBothExpensesSuppliedError
    extends MtdError("RULE_BOTH_EXPENSES_SUPPLIED", "Both Expenses and Consolidated Expenses must not be present at the same time")
object RuleBothAllowancesSuppliedError
  extends MtdError("RULE_BOTH_ALLOWANCES_SUPPLIED", "Both allowances and property allowances must not be present at the same time")
object RuleBuildingNameNumberError        extends MtdError("RULE_BUILDING_NAME_NUMBER", "Postcode must be supplied along with at least one of name or number")
object RuleToDateBeforeFromDateError      extends MtdError("RULE_TO_DATE_BEFORE_FROM_DATE", "The To date cannot be earlier than the From date")
object RuleCountryCodeError               extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code")
object RuleOverlappingPeriodError         extends MtdError("RULE_OVERLAPPING_PERIOD", "Period summary overlaps with any of the existing period summaries")
object RuleMisalignedPeriodError          extends MtdError("RULE_MISALIGNED_PERIOD", "Period summary is not within the accounting period")
object RuleNotContiguousPeriodError       extends MtdError("RULE_NOT_CONTIGUOUS_PERIOD", "Period summaries are not contiguous")
object RuleTaxYearNotSupportedError       extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified is before the minimum tax year value")
object RuleTaxYearRangeInvalidError       extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "The tax year range is invalid")
object RuleDuplicateSubmissionError       extends MtdError("RULE_DUPLICATE_SUBMISSION", "A summary has already been submitted for the period specified")
object RuleTypeOfBusinessIncorrectError   extends MtdError("RULE_TYPE_OF_BUSINESS_INCORRECT", "The businessId is for a different type of business")

object MissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing")
object MissingToDateError   extends MtdError("MISSING_TO_DATE", "The To date parameter is missing")

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object SubmissionIdNotFoundError extends MtdError("SUBMISSION_ID_NOT_FOUND", "Submission ID not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError       extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
