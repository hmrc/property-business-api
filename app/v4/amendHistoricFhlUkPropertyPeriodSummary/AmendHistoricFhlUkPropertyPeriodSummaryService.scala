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

package v4.amendHistoricFhlUkPropertyPeriodSummary

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v4.amendHistoricFhlUkPropertyPeriodSummary.model.request.AmendHistoricFhlUkPiePeriodSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendHistoricFhlUkPropertyPeriodSummaryService @Inject() (connector: AmendHistoricFhlUkPropertyPeriodSummaryConnector) extends BaseService {

  def amend(request: AmendHistoricFhlUkPiePeriodSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext
  ): Future[ServiceOutcome[Unit]] = {

    connector.amend(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO" -> NinoFormatError,
      "INVALID_TYPE" -> InternalError,
      "INVALID_PAYLOAD" -> InternalError,
      "INVALID_DATE_FROM" -> PeriodIdFormatError,
      "INVALID_DATE_TO" -> PeriodIdFormatError,
      "INVALID_CORRELATIONID" -> InternalError,
      "SUBMISSION_PERIOD_NOT_FOUND" -> NotFoundError,
      "NOT_FOUND_PROPERTY" -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
      "NOT_FOUND" -> NotFoundError,
      "BOTH_EXPENSES_SUPPLIED" -> RuleBothExpensesSuppliedError,
      "SERVER_ERROR" -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

}
