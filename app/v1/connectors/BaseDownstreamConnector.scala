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
import play.api.libs.json.Writes
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector extends Logging {
  val http: HttpClient
  val appConfig: AppConfig

  private[connectors] def ifsHeaderCarrier(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer ${appConfig.ifsToken}")))
      .withExtraHeaders("Environment" -> appConfig.ifsEnv, "CorrelationId" -> correlationId)

  def post[Body: Writes, Resp](body: Body, uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                                              hc: HeaderCarrier,
                                                              httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                              correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.POST(s"${appConfig.ifsBaseUrl}/${uri.value}", body)
    }

    doPost(ifsHeaderCarrier(hc, correlationId))
  }

  def get[Resp](uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                   hc: HeaderCarrier,
                                   httpReads: HttpReads[DownstreamOutcome[Resp]],
                                   correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http.GET(s"${appConfig.ifsBaseUrl}/${uri.value}")

    doGet(ifsHeaderCarrier(hc, correlationId))
  }

  def put[Body: Writes, Resp](body: Body, uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                                             hc: HeaderCarrier,
                                                             httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                             correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http.PUT(s"${appConfig.ifsBaseUrl}/${uri.value}", body)

    doPut(ifsHeaderCarrier(hc, correlationId))
  }

  def delete[Resp](uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      httpReads: HttpReads[DownstreamOutcome[Resp]],
                                      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http.DELETE(s"${appConfig.ifsBaseUrl}/${uri.value}")

    doDelete(ifsHeaderCarrier(hc, correlationId))
  }

}