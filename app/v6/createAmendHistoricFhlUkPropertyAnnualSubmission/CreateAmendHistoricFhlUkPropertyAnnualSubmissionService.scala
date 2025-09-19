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

package v6.createAmendHistoricFhlUkPropertyAnnualSubmission

import cats.implicits.*
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request.CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionService @Inject() (connector: CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector)
    extends BaseService {

  def amend(request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse]] = {

    connector.amend(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"           -> NinoFormatError,
      "INVALID_TYPE"           -> InternalError,
      "INVALID_TAX_YEAR"       -> TaxYearFormatError,
      "INVALID_PAYLOAD"        -> InternalError,
      "INVALID_CORRELATIONID"  -> InternalError,
      "NOT_FOUND_PROPERTY"     -> NotFoundError,
      "NOT_FOUND"              -> NotFoundError,
      "GONE"                   -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"           -> InternalError,
      "SERVICE_UNAVAILABLE"    -> InternalError
    )

}
