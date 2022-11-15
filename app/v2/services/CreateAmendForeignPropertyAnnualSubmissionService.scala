/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.implicits._
import cats.data.EitherT
import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.CreateAmendForeignPropertyAnnualSubmissionConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionRequest
import v2.support.DownstreamResponseMappingSupport

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionService @Inject()(connector: CreateAmendForeignPropertyAnnualSubmissionConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAmendForeignPropertyAnnualSubmission(request: CreateAmendForeignPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.createAmendForeignPropertyAnnualSubmission(request)).leftMap(mapDownstreamErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
      "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
      "INVALID_TAX_YEAR"            -> TaxYearFormatError,
      "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
      "BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
      "INCOME_SOURCE_NOT_FOUND"     -> NotFoundError,
      "MISSING_ALLOWANCES"          -> InternalError,
      "INVALID_PAYLOAD"             -> InternalError,
      "INVALID_CORRELATIONID"       -> InternalError,
      "DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
      "SERVER_ERROR"                -> InternalError,
      "SERVICE_UNAVAILABLE"         -> InternalError
    )
}
