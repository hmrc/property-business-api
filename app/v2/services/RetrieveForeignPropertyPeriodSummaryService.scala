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

import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult}
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
import v2.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse
import v2.support.IfsResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyPeriodSummaryService @Inject()(connector: RetrieveForeignPropertyPeriodSummaryConnector)
  extends IfsResponseMappingSupport with Logging {

  def retrieveForeignProperty(request: RetrieveForeignPropertyPeriodSummaryRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[ServiceOutcome[RetrieveForeignPropertyPeriodSummaryResponse]] = {

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
      "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "NO_DATA_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

  private def validateBusinessType(resultWrapper: ResponseWrapper[connectors.RetrieveForeignPropertyPeriodSummaryConnector.Result]) =
    resultWrapper.responseData match {
      case ForeignResult(response) => Right(ResponseWrapper(resultWrapper.correlationId, response))
      case NonForeignResult        => Left(ErrorWrapper(resultWrapper.correlationId, RuleTypeOfBusinessIncorrectError))
    }

}