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

package v4.amendForeignPropertyPeriodSummary

import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import cats.implicits._
import common.models.errors.{RuleDuplicateCountryCodeError, RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import v4.amendForeignPropertyPeriodSummary.model.request.AmendForeignPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendForeignPropertyPeriodSummaryService @Inject() (connector: AmendForeignPropertyPeriodSummaryConnector) extends BaseService {

  def amendForeignPropertyPeriodSummary(request: AmendForeignPropertyPeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext
  ): Future[ServiceOutcome[Unit]] = {

    connector.amendForeignPropertyPeriodSummary(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
      "INVALID_TAX_YEAR"            -> InternalError,
      "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
      "INVALID_SUBMISSION_ID"       -> SubmissionIdFormatError,
      "INVALID_PAYLOAD"             -> InternalError,
      "INVALID_CORRELATIONID"       -> InternalError,
      "NO_DATA_FOUND"               -> NotFoundError,
      "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
      "MISSING_EXPENSES"            -> InternalError,
      "BUSINESS_VALIDATION_FAILURE" -> InternalError,
      "DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
      "SERVER_ERROR"                -> InternalError,
      "SERVICE_UNAVAILABLE"         -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"       -> InternalError,
      "INCOME_SOURCE_NOT_COMPATIBLE" -> RuleTypeOfBusinessIncorrectError
    )

    errors ++ extraTysErrors
  }

}
