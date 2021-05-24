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

package v1.connectors

import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.connectors.httpparsers.StandardDesHttpParser._
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRequest
import v1.models.response.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListForeignPropertiesPeriodSummariesConnector @Inject()(val http: HttpClient,
                                                              val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listForeignProperties(request: ListForeignPropertiesPeriodSummariesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DownstreamOutcome[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]]] = {

    val url = s"income-tax/business/property/${request.nino.nino}/${request.businessId}/period?fromDate=${request.fromDate}&toDate=${request.toDate}"

    get(
      uri = IfsUri[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]](url)
    )
  }
}