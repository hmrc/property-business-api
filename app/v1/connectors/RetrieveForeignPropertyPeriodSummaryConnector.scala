/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.connectors.httpparsers.StandardDesHttpParser._
import v1.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyRequestData
import v1.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyPeriodSummaryConnector @Inject()(val http: HttpClient,
                                                              val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveForeignProperty(request: RetrieveForeignPropertyRequestData)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[DesOutcome[RetrieveForeignPropertyResponse]] = {

    get(
      uri = DesUri[RetrieveForeignPropertyResponse](s"business/property/${request.nino}/${request.businessId}/period/${request.submissionId}")
    )
  }
}
