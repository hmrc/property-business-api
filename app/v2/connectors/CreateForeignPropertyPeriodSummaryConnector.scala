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

package v2.connectors

import config.AppConfig

import javax.inject.{ Inject, Singleton }
import play.api.http.Status
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient }
import v2.connectors.DownstreamUri.IfsUri
import v2.connectors.httpparsers.StandardHttpParser._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequest
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateForeignPropertyPeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createForeignProperty(request: CreateForeignPropertyPeriodSummaryRequest)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(Status.OK)

    post(
      body = request.body,
      uri = IfsUri[CreateForeignPropertyPeriodSummaryResponse](
        s"income-tax/business/property/periodic?taxableEntityId=${request.nino.nino}&taxYear=${request.taxYear}&incomeSourceId=${request.businessId}")
    )
  }
}
