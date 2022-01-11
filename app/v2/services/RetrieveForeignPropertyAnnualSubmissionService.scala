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
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult}
import v2.controllers.EndpointLogContext
import v2.models.errors.{BusinessIdFormatError, DownstreamError, ErrorWrapper, NinoFormatError, NotFoundError, RuleTaxYearNotSupportedError, RuleTypeOfBusinessIncorrectError, TaxYearFormatError}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v2.support.IfsResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyAnnualSubmissionService @Inject()(connector: RetrieveForeignPropertyAnnualSubmissionConnector)
  extends IfsResponseMappingSupport with Logging {

  def retrieveForeignProperty(request: RetrieveForeignPropertyAnnualSubmissionRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[ServiceOutcome[RetrieveForeignPropertyAnnualSubmissionResponse]] = {

    val result = for {
      connectorResultWrapper <- EitherT(connector.retrieveForeignProperty(request)).leftMap(mapIfsErrors(ifsErrorMap))
      mtdResponseWrapper     <- EitherT.fromEither[Future](validateBusinessType(connectorResultWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "NO_DATA_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )


  private def validateBusinessType(resultWrapper: ResponseWrapper[connectors.RetrieveForeignPropertyAnnualSubmissionConnector.Result]) =
    resultWrapper.responseData match {
      case ForeignResult(response) => Right(ResponseWrapper(resultWrapper.correlationId, response))
      case NonForeignResult        => Left(ErrorWrapper(resultWrapper.correlationId, RuleTypeOfBusinessIncorrectError))
    }
}
