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

package v6.deletePropertyAnnualSubmission

import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import cats.implicits._
import common.models.errors.RuleOutsideAmendmentWindowError
import v6.deletePropertyAnnualSubmission.model.request.DeletePropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePropertyAnnualSubmissionService @Inject() (connector: DeletePropertyAnnualSubmissionConnector) extends BaseService {

  def deletePropertyAnnualSubmission(
      request: DeletePropertyAnnualSubmissionRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.deletePropertyAnnualSubmission(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val downstreamErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError,
      "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"  -> InternalError,
      "NOT_FOUND"               -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"  -> RuleTaxYearNotSupportedError
    )

    downstreamErrors ++ extraTysErrors
  }

}
