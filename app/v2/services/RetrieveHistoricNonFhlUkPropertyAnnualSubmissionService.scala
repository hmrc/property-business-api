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

import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
import v2.controllers.EndpointLogContext
import v2.models.errors.{ InternalError, MtdError, NinoFormatError, NotFoundError, RuleHistoricTaxYearNotSupportedError, TaxYearFormatError }
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionService @Inject()(connector: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"            -> NinoFormatError,
      "INVALID_TYPE"            -> InternalError,
      "INVALID_TAX_YEAR"        -> TaxYearFormatError,
      "INVALID_CORRELATIONID"   -> InternalError,
      "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
      "NOT_FOUND_PERIOD"        -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

  def retrieve(request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]] = {

    val result = for {
      resultWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield resultWrapper

    result.value
  }

}
