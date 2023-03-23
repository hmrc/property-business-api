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

import cats.implicits._
import cats.data.EitherT

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.CreateForeignPropertyPeriodSummaryConnector
import api.controllers.EndpointLogContext
import api.models.errors._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequest
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse
import api.services.{DownstreamResponseMappingSupport, ServiceOutcome}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryService @Inject()(connector: CreateForeignPropertyPeriodSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[CreateForeignPropertyPeriodSummaryResponse]] = {

    EitherT(connector.createForeignProperty(request)).leftMap(mapDownstreamErrors(downstreamErrorMap)).value
  }

  private def downstreamErrorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "DUPLICATE_COUNTRY_CODE"    -> RuleDuplicateCountryCodeError,
      "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
      "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
      "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
      "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError,
      "INVALID_PAYLOAD"           -> InternalError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmissionError,
      "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError,
      "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "MISSING_EXPENSES"          -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"  -> InternalError,
      "PERIOD_NOT_ALIGNED"      -> RuleMisalignedPeriodError,
      "PERIOD_OVERLAPS"         -> RuleOverlappingPeriodError,
    )

    errors ++ extraTysErrors
  }

}
