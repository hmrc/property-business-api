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

package v5.createAmendUkPropertyAnnualSubmission

import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import cats.implicits._
import common.models.errors.{RulePropertyIncomeAllowanceError, RuleTypeOfBusinessIncorrectError}
import v5.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkPropertyAnnualSubmissionService @Inject() (connector: CreateAmendUkPropertyAnnualSubmissionConnector) extends BaseService {

  def createAmendUkPropertyAnnualSubmission(
      request: CreateAmendUkPropertyAnnualSubmissionRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.createAmendUkPropertyAnnualSubmission(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
      "INVALID_TAX_YEAR"            -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
      "INVALID_PAYLOAD"             -> InternalError,
      "INVALID_CORRELATIONID"       -> InternalError,
      "INCOME_SOURCE_NOT_FOUND"     -> NotFoundError,
      "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
      "BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
      "MISSING_ALLOWANCES"          -> InternalError,
      "DUPLICATE_COUNTRY_CODE"      -> InternalError,
      "SERVER_ERROR"                -> InternalError,
      "SERVICE_UNAVAILABLE"         -> InternalError
    )

    val extraTysErrors = Map(
      "MISSING_EXPENSES" -> InternalError,
      "FIELD_CONFLICT"   -> RulePropertyIncomeAllowanceError
    )

    errors ++ extraTysErrors
  }

}
