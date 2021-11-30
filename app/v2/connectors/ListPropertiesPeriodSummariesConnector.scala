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

package v2.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.connectors.httpparsers.StandardIfsHttpParser._
import v2.models.request.listPropertiesPeriodSummaries.ListPropertiesPeriodSummariesRequest
import v2.models.response.listPropertiesPeriodSummaries.ListPropertiesPeriodSummariesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPropertiesPeriodSummariesConnector @Inject()(val http: HttpClient,
                                                       val appConfig: AppConfig) extends BaseIfsConnector {

  def listPeriodSummaries(request: ListPropertiesPeriodSummariesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[IfsOutcome[ListPropertiesPeriodSummariesResponse]] = {

    val url = s"income-tax/business/property/${request.nino.nino}/${request.businessId}/period"

    get(
      uri = IfsUri[ListPropertiesPeriodSummariesResponse](url),
      queryParams = Seq("taxYear" -> request.taxYear)
    )
  }
}