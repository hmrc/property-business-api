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

package v6.deleteHistoricNonFhlUkPropertyAnnualSubmission

import cats.implicits._
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import shared.controllers.RequestContext
import shared.models.errors.{InternalError, NinoFormatError, NotFoundError, TaxYearFormatError}
import shared.services.{BaseService, ServiceOutcome}
import v6.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteHistoricNonFhlUkPropertyAnnualSubmissionService @Inject() (connector: DeleteHistoricNonFhlUkPropertyAnnualSubmissionConnector)
    extends BaseService {

  def deleteHistoricUkPropertyAnnualSubmission(
      request: DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
  )(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

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
