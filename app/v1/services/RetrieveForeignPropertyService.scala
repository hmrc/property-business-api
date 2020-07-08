/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.services

import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveForeignPropertyConnector
import v1.controllers.EndpointLogContext
import v1.models.errors.{BusinessIdFormatError, DownstreamError, NinoFormatError, NotFoundError, SubmissionIdFormatError, SubmissionIdNotFoundError}
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyService @Inject()(connector: RetrieveForeignPropertyConnector) extends DesResponseMappingSupport with Logging {

  def createForeignProperty(request: RetrieveForeignPropertyRequestData)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[RetrieveForeignPropertyServiceOutcome] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveForeignProperty(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_BUSINESS_ID" -> BusinessIdFormatError,
      "FORMAT_SUBMISSION_ID" -> SubmissionIdFormatError,
      "NOT_FOUND" -> NotFoundError,
      "SUBMISSION_ID_NOT_FOUND" -> SubmissionIdNotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

}
