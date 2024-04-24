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

package v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import api.controllers.RequestContext
import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, RuleHistoricTaxYearNotSupportedError, TaxYearFormatError}
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.response.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionService @Inject() (connector: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector)
    extends BaseService {

  def retrieve(request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]] = {

    connector.retrieve(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"            -> NinoFormatError,
      "INVALID_TYPE"            -> InternalError,
      "INVALID_TAX_YEAR"        -> TaxYearFormatError,
      "INVALID_CORRELATIONID"   -> InternalError,
      "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
      "NOT_FOUND_PERIOD"        -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

}
