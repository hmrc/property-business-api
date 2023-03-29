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

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.data.EitherT
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector.{NonUkResult, UkResult}
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveUkPropertyPeriodSummaryService @Inject() (connector: RetrieveUkPropertyPeriodSummaryConnector) extends BaseService {

  def retrieveUkProperty(request: RetrieveUkPropertyPeriodSummaryRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[RetrieveUkPropertyPeriodSummaryServiceOutcome] = {

    val result = for {
      connectorResultWrapper <- EitherT(connector.retrieveUkProperty(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper     <- EitherT.fromEither[Future](validateBusinessType(connectorResultWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errorMap = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError,
      "INVALID_CORRELATIONID"     -> InternalError
    )

    val tysErrorMap =
      Map(
        "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
        "INVALID_CORRELATION_ID"  -> InternalError
      )

    errorMap ++ tysErrorMap
  }

  private def validateBusinessType(resultWrapper: ResponseWrapper[RetrieveUkPropertyPeriodSummaryConnector.Result]) =
    resultWrapper.responseData match {
      case UkResult(response) => Right(ResponseWrapper(resultWrapper.correlationId, response))
      case NonUkResult        => Left(ErrorWrapper(resultWrapper.correlationId, RuleTypeOfBusinessIncorrectError))
    }

}
