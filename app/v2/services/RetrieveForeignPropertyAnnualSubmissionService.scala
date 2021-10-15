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

package v2.services

import cats.implicits._
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector
import v2.controllers.EndpointLogContext
import v2.models.errors.{BusinessIdFormatError, DownstreamError, NinoFormatError, NotFoundError}
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.support.IfsResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyAnnualSubmissionService @Inject()(connector: RetrieveForeignPropertyAnnualSubmissionConnector)
  extends IfsResponseMappingSupport with Logging {

  def retrieveForeignProperty(request: RetrieveForeignPropertyAnnualSubmissionRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[RetrieveForeignPropertyAnnualSubmissionServiceOutcome] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.retrieveForeignProperty(request)).leftMap(mapIfsErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
      "NO_DATA_FOUND" -> NotFoundError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "INVALID_PAYLOAD" -> DownstreamError,
      "INVALID_TAX_YEAR" -> DownstreamError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

}
