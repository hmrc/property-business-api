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

package v6.retrieveForeignPropertyPeriodSummary

import shared.controllers.RequestContext
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import common.models.errors.{RuleTypeOfBusinessIncorrectError, SubmissionIdFormatError}
import v6.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult}
import v6.retrieveForeignPropertyPeriodSummary.model.request.RetrieveForeignPropertyPeriodSummaryRequestData
import v6.retrieveForeignPropertyPeriodSummary.model.response.RetrieveForeignPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyPeriodSummaryService @Inject() (connector: RetrieveForeignPropertyPeriodSummaryConnector) extends BaseService {

  def retrieveForeignProperty(request: RetrieveForeignPropertyPeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveForeignPropertyPeriodSummaryResponse]] = {

    val result = EitherT(connector.retrieveForeignProperty(request))
      .leftMap(mapDownstreamErrors(downstreamErrorMap))
      .flatMap(connectorResultWrapper => EitherT.fromEither(validateBusinessType(connectorResultWrapper)))

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val downstreamErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> InternalError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"  -> InternalError
    )

    downstreamErrors ++ extraTysErrors
  }

  private def validateBusinessType(resultWrapper: ResponseWrapper[RetrieveForeignPropertyPeriodSummaryConnector.Result]) =
    resultWrapper.responseData match {
      case ForeignResult(response) => Right(ResponseWrapper(resultWrapper.correlationId, response))
      case NonForeignResult        => Left(ErrorWrapper(resultWrapper.correlationId, RuleTypeOfBusinessIncorrectError))
    }

}
