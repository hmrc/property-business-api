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

import cats.implicits._
import cats.data.EitherT

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.CreateForeignPropertyPeriodSummaryConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequest
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse
import v2.support.IfsResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryService @Inject()(connector: CreateForeignPropertyPeriodSummaryConnector)
  extends IfsResponseMappingSupport with Logging {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[ServiceOutcome[CreateForeignPropertyPeriodSummaryResponse]] = {

    val result = for {
      ifsResponseWrapper <- EitherT(connector.createForeignProperty(request)).leftMap(mapIfsErrors(ifsErrorMap))
    } yield ifsResponseWrapper

    result.value
  }

  private def ifsErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "DUPLICATE_COUNTRY_CODE" -> RuleDuplicateCountryCodeError,
      "OVERLAPS_IN_PERIOD" -> RuleOverlappingPeriodError,
      "NOT_ALIGN_PERIOD" -> RuleMisalignedPeriodError,
      "GAPS_IN_PERIOD" -> RuleNotContiguousPeriodError,
      "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamMtdError,
      "SERVICE_UNAVAILABLE" -> DownstreamMtdError,
      "INVALID_PAYLOAD" -> DownstreamMtdError,
      "INVALID_CORRELATIONID" -> DownstreamMtdError,
      "DUPLICATE_SUBMISSION" -> RuleDuplicateSubmissionError,
      "INVALID_DATE_RANGE" -> RuleToDateBeforeFromDateError,
      "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrectError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "MISSING_EXPENSES" -> DownstreamMtdError
    )

}
