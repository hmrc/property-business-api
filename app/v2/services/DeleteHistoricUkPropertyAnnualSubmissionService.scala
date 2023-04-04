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
import api.models.errors.{InternalError, NinoFormatError, NotFoundError, RuleHistoricTaxYearNotSupportedError, TaxYearFormatError}
import api.services.BaseService
import cats.implicits._
import v2.connectors.DeleteHistoricUkPropertyAnnualSubmissionConnector
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionService @Inject() (connector: DeleteHistoricUkPropertyAnnualSubmissionConnector) extends BaseService {

  def deleteHistoricUkPropertyAnnualSubmission(request: DeleteHistoricUkPropertyAnnualSubmissionRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[DeleteHistoricUkPropertyAnnualSubmissionServiceOutcome] = {

    connector.deleteHistoricUkPropertyAnnualSubmission(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap =
    Map(
      "INVALID_NINO"           -> NinoFormatError,
      "INVALID_TAX_YEAR"       -> TaxYearFormatError,
      "INVALID_TYPE"           -> InternalError,
      "INVALID_PAYLOAD"        -> InternalError,
      "INVALID_CORRELATIONID"  -> InternalError,
      "NOT_FOUND"              -> NotFoundError,
      "NOT_FOUND_PROPERTY"     -> NotFoundError,
      "GONE"                   -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"           -> InternalError,
      "SERVICE_UNAVAILABLE"    -> InternalError
    )

}
