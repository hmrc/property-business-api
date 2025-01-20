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

package v6.retrieveUkPropertyCumulativeSummary

import shared.controllers.RequestContext
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import RetrieveUkPropertyCumulativeSummaryConnector.{NonUkResult, UkResult}
import common.models.errors.RuleTypeOfBusinessIncorrectError
import v6.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData
import v6.retrieveUkPropertyCumulativeSummary.model.response.RetrieveUkPropertyCumulativeSummaryResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveUkPropertyCumulativeSummaryService @Inject() (connector: RetrieveUkPropertyCumulativeSummaryConnector) extends BaseService {

  def retrieveUkProperty(request: RetrieveUkPropertyCumulativeSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveUkPropertyCumulativeSummaryResponse]] = {

    val result = EitherT(connector.retrieveUkPropertyCumulativeSummary(request))
      .leftMap(mapDownstreamErrors(downstreamErrorMap))
      .flatMap(connectorResultWrapper => EitherT.fromEither(validateBusinessType(connectorResultWrapper)))

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"    -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "NOT_FOUND"                 -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

  private def validateBusinessType(resultWrapper: ResponseWrapper[RetrieveUkPropertyCumulativeSummaryConnector.Result]) =
    resultWrapper.responseData match {
      case UkResult(response) => Right(ResponseWrapper(resultWrapper.correlationId, response))
      case NonUkResult        => Left(ErrorWrapper(resultWrapper.correlationId, RuleTypeOfBusinessIncorrectError))
    }

}
