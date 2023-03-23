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

import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import api.controllers.EndpointLogContext
import api.models.errors._
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse
import api.services.{DownstreamResponseMappingSupport, ServiceOutcome}
import v2.connectors.CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionService @Inject()(connector: CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def amend(request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse]] = {

    val result = for {
      responseWrapper <- EitherT(connector.amend(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield responseWrapper

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"           -> NinoFormatError,
      "INVALID_TYPE"           -> InternalError,
      "INVALID_TAX_YEAR"       -> TaxYearFormatError,
      "INVALID_PAYLOAD"        -> InternalError,
      "INVALID_CORRELATIONID"  -> InternalError,
      "NOT_FOUND_PROPERTY"     -> NotFoundError,
      "NOT_FOUND"              -> NotFoundError,
      "GONE"                   -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"           -> InternalError,
      "SERVICE_UNAVAILABLE"    -> InternalError
    )
}
