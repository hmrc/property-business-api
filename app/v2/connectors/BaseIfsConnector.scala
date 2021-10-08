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
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseIfsConnector extends Logging {
  val http: HttpClient
  val appConfig: AppConfig

  private def ifsHeaderCarrier(additionalHeaders: Seq[String] = Seq.empty)(implicit hc: HeaderCarrier,
                                                                           correlationId: String): HeaderCarrier =
    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${appConfig.ifsToken}",
          "Environment" -> appConfig.ifsEnv,
          "CorrelationId" -> correlationId
        ) ++
        // Other headers (i.e Gov-Test-Scenario, Content-Type)
        hc.headers(additionalHeaders ++ appConfig.ifsEnvironmentHeaders.getOrElse(Seq.empty))
    )

  def post[Body: Writes, Resp](body: Body, uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                                              hc: HeaderCarrier,
                                                              httpReads: HttpReads[IfsOutcome[Resp]],
                                                              correlationId: String): Future[IfsOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[IfsOutcome[Resp]] = {
      http.POST(url = s"${appConfig.ifsBaseUrl}/${uri.value}", body)
    }

    doPost(ifsHeaderCarrier(Seq("Content-Type")))
  }

  def get[Resp](uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                   hc: HeaderCarrier,
                                   httpReads: HttpReads[IfsOutcome[Resp]],
                                   correlationId: String): Future[IfsOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[IfsOutcome[Resp]] =
      http.GET(url = s"${appConfig.ifsBaseUrl}/${uri.value}")

    doGet(ifsHeaderCarrier())
  }

  def put[Body: Writes, Resp](body: Body, uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                                             hc: HeaderCarrier,
                                                             httpReads: HttpReads[IfsOutcome[Resp]],
                                                             correlationId: String): Future[IfsOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[IfsOutcome[Resp]] =
      http.PUT(url = s"${appConfig.ifsBaseUrl}/${uri.value}", body)

    doPut(ifsHeaderCarrier(Seq("Content-Type")))
  }

  def delete[Resp](uri: IfsUri[Resp])(implicit ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      httpReads: HttpReads[IfsOutcome[Resp]],
                                      correlationId: String): Future[IfsOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[IfsOutcome[Resp]] =
      http.DELETE(url = s"${appConfig.ifsBaseUrl}/${uri.value}")

    doDelete(ifsHeaderCarrier())
  }

}