/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.AmendUkPropertyPeriodSummaryConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.amendUkPropertyPeriodSummary.AmendUkPropertyPeriodSummaryRequest
import v2.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendUkPropertyPeriodSummaryService @Inject()(connector: AmendUkPropertyPeriodSummaryConnector)
  extends DownstreamResponseMappingSupport with Logging {

  def amendUkPropertyPeriodSummary(request: AmendUkPropertyPeriodSummaryRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.amendUkPropertyPeriodSummary(request)).leftMap(mapDownstreamErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
      "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
      "INVALID_PAYLOAD" -> DownstreamMtdError,
      "INVALID_CORRELATIONID" -> DownstreamMtdError,
      "NO_DATA_FOUND" -> NotFoundError,
      "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "BUSINESS_VALIDATION_FAILURE" -> DownstreamMtdError,
      "DUPLICATE_COUNTRY_CODE" -> DownstreamMtdError,
      "MISSING_EXPENSES" -> DownstreamMtdError,
      "SERVER_ERROR" -> DownstreamMtdError,
      "SERVICE_UNAVAILABLE" -> DownstreamMtdError
    )
}
