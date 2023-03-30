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
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector
import v2.connectors.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult}
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
import v2.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyPeriodSummaryService @Inject() (connector: RetrieveForeignPropertyPeriodSummaryConnector) extends BaseService {

  def retrieveForeignProperty(request: RetrieveForeignPropertyPeriodSummaryRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveForeignPropertyPeriodSummaryResponse]] = {

    val result = for {
      connectorResultWrapper <- connector.retrieveForeignProperty(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
      mtdResponseWrapper     <- Future.successful(connectorResultWrapper.flatMap(wrappedResult => validateBusinessType(wrappedResult)))

    } yield mtdResponseWrapper

    result
  }

  private val errorMap = {
    val downstreamErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
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
