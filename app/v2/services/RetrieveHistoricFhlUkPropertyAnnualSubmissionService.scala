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
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.{RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector}
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricFhlUkPropertyAnnualSubmissionService @Inject()(connector: RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  private val downstreamErrorMap =
    Map(
      "INVALID_NINO"        -> NinoFormatError,
      "INVALID_TYPE"        -> DownstreamMtdError,
      "INVALID_TAX_YEAR"    -> TaxYearFormatError,
      "NOT_FOUND_PROPERTY"  -> NotFoundError,
      "NOT_FOUND_PERIOD"    -> NotFoundError,
      "SERVER_ERROR"        -> DownstreamMtdError,
      "SERVICE_UNAVAILABLE" -> DownstreamMtdError
    )

  def retrieve(request: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]] = {

    val result = for {
      resultWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield resultWrapper

    result.value
  }

}
