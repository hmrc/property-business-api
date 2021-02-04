/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.services

import cats.implicits._
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateForeignPropertyPeriodSummaryConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequest
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryService @Inject()(connector: CreateForeignPropertyPeriodSummaryConnector) extends DesResponseMappingSupport with Logging {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[CreateForeignPropertyPeriodSummaryServiceOutcome] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.createForeignProperty(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_BUSINESS_ID" -> BusinessIdFormatError,
      "RULE_OVERLAPPING_PERIOD" -> RuleOverlappingPeriodError,
      "RULE_MISALIGNED_PERIOD" -> RuleMisalignedPeriodError,
      "RULE_NOT_CONTIGUOUS_PERIOD" -> RuleNotContiguousPeriodError,
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

}
