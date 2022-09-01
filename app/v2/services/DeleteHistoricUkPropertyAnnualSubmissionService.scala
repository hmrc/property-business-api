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
import v2.connectors.DeleteHistoricUkPropertyAnnualSubmissionConnector
import v2.controllers.EndpointLogContext
import v2.models.errors.{ InternalError, NinoFormatError, NotFoundError, TaxYearFormatError }
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequest
import v2.support.DownstreamResponseMappingSupport

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionService @Inject()(connector: DeleteHistoricUkPropertyAnnualSubmissionConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def deleteHistoricUkPropertyAnnualSubmission(request: DeleteHistoricUkPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.deleteHistoricUkPropertyAnnualSubmission(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_NINO"          -> NinoFormatError,
      "INVALID_TAX_YEAR"      -> TaxYearFormatError,
      "INVALID_TYPE"          -> InternalError,
      "INVALID_PAYLOAD"       -> InternalError,
      "INVALID_CORRELATIONID" -> InternalError,
      "NOT_FOUND"             -> NotFoundError,
      "NOT_FOUND_PROPERTY"    -> NotFoundError,
      "GONE"                  -> NotFoundError,
      "UNPROCESSABLE_ENTTY"   -> InternalError,
      "SERVER_ERROR"          -> InternalError,
      "SERVICE_UNAVAILABLE"   -> InternalError
    )

}
