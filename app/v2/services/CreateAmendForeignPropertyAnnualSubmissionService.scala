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

package v2.services

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v2.connectors.CreateAmendForeignPropertyAnnualSubmissionConnector
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionService @Inject() (connector: CreateAmendForeignPropertyAnnualSubmissionConnector)
    extends BaseService {

  def createAmendForeignPropertyAnnualSubmission(request: CreateAmendForeignPropertyAnnualSubmissionRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.createAmendForeignPropertyAnnualSubmission(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
      "MISSING_ALLOWANCES"        -> InternalError,
      "INVALID_PAYLOAD"           -> InternalError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "DUPLICATE_COUNTRY_CODE"    -> RuleDuplicateCountryCodeError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "MISSING_EXPENSES" -> InternalError,
      "FIELD_CONFLICT"   -> RulePropertyIncomeAllowanceError
    )

    errors ++ extraTysErrors
  }

}
