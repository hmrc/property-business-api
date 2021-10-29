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
import mocks.MockAppConfig
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient, HttpReads }
import v2.mocks.MockHttpClient
import v2.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class BaseIfsConnectorSpec extends ConnectorSpec {
  // WLOG
  case class Result(value: Int)

  // WLOG
  val body    = "body"
  val outcome = Right(ResponseWrapper(correlationId, Result(2)))

  val url         = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  implicit val httpReads: HttpReads[IfsOutcome[Result]] = mock[HttpReads[IfsOutcome[Result]]]

  class Test(ifsEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseIfsConnector = new BaseIfsConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns ifsEnvironmentHeaders
  }

  "BaseIfsConnector" when {
    val queryParams = Seq(
      "nino" -> "AA123456Q"
    )

    val requiredHeaders: Seq[(String, String)] = Seq(
      "Environment"       -> "ifs-environment",
      "Authorization"     -> s"Bearer ifs-token",
      "User-Agent"        -> "property-business-api",
      "CorrelationId"     -> correlationId,
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    val excludedHeaders: Seq[(String, String)] = Seq(
      "AnotherHeader" -> "HeaderValue"
    )

    "making a HTTP request to IFS" must {
      testHttpMethods(
        dummyIfsHeaderCarrierConfig,
        queryParams = queryParams,
        requiredHeaders = requiredHeaders,
        excludedHeaders = excludedHeaders,
        ifsEnvironmentHeaders = Some(allowedIfsHeaders)
      )

      "exclude all `otherHeaders` when no external service header allow-list is found" should {
        val requiredHeaders: Seq[(String, String)] = Seq(
          "Environment"   -> "ifs-environment",
          "Authorization" -> s"Bearer ifs-token",
          "User-Agent"    -> "property-business-api",
          "CorrelationId" -> correlationId,
        )

        testHttpMethods(dummyIfsHeaderCarrierConfig,
                        queryParams = queryParams,
                        requiredHeaders = requiredHeaders,
                        excludedHeaders = otherHeaders,
                        ifsEnvironmentHeaders = None)
      }
    }
  }

  def testHttpMethods(config: HeaderCarrier.Config,
                      queryParams: Seq[(String, String)],
                      requiredHeaders: Seq[(String, String)],
                      excludedHeaders: Seq[(String, String)],
                      ifsEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "GET" in new Test(ifsEnvironmentHeaders) {
        MockHttpClient
          .get(absoluteUrl, config, queryParams = queryParams, requiredHeaders = requiredHeaders, excludedHeaders = excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.get(IfsUri[Result](url), queryParams = queryParams)) shouldBe outcome
      }

      "DELETE" in new Test(ifsEnvironmentHeaders) {
        MockHttpClient
          .delete(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.delete(IfsUri[Result](url))) shouldBe outcome
      }

      "POST" in new Test(ifsEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, IfsUri[Result](url))) shouldBe outcome
      }

      "PUT" in new Test(ifsEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(absoluteUrl, config, body, requiredHeadersPut, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.put(body, IfsUri[Result](url))) shouldBe outcome
      }
    }
  }
}
