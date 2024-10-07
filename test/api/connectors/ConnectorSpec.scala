/*
 * Copyright 2023 HM Revenue & Customs
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

package api.connectors

import com.google.common.base.Charsets
import config.{BasicAuthDownstreamConfig, DownstreamConfig, MockAppConfig}
import mocks.MockHttpClient
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import utils.UrlUtils

import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl                   = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputHeaders: Seq[(String, String)] = List(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = inputHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      List("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("this-api")
    )

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    protected def requiredHeaders: Seq[(String, String)]
    protected def excludedHeaders: Seq[(String, String)] = List("AnotherHeader" -> "HeaderValue")

    protected val allowedHeaders: Seq[String] = List("Gov-Test-Scenario")

    protected def intent: Option[String] = None

    protected def willGet[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockHttpClient
        .get(
          url = url,
          parameters = parameters,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        )
    }

    protected def willPost[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .post(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ List("Content-Type" -> "application/json"),
          excludedHeaders = excludedHeaders
        )
    }

    protected def willPut[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ List("Content-Type" -> "application/json"),
          excludedHeaders = excludedHeaders
        )
    }

    protected def willDelete[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      val fullUrl = UrlUtils.appendQueryParams(url, parameters)

      MockHttpClient
        .delete(
          url = fullUrl,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        )
    }

  }

  protected trait StandardConnectorTest extends ConnectorTest {
    protected def name: String

    private val token       = s"$name-token"
    private val environment = s"$name-environment"

    protected def requiredHeaders: Seq[(String, String)] = List(
      "Authorization"        -> s"Bearer $token",
      "Environment"          -> environment,
      "User-Agent"           -> "this-api",
      "CorrelationId"        -> correlationId,
      "Gov-Test-Scenario"    -> "DEFAULT"
    ) ++ intent.map("intent" -> _)

    protected final val config: DownstreamConfig = DownstreamConfig(this.baseUrl, environment, token, Some(allowedHeaders))
  }

  protected trait DesTest extends StandardConnectorTest {
    val name = "des"

    MockedAppConfig.desDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait IfsTest extends StandardConnectorTest {
    override val name = "ifs"

    MockedAppConfig.ifsDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait TysIfsTest extends StandardConnectorTest {
    override val name = "tys-ifs"

    MockedAppConfig.tysIfsDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait HipTest extends ConnectorTest {
    private val clientId     = "clientId"
    private val clientSecret = "clientSecret"

    private val token =
      Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(Charsets.UTF_8))

    private val environment = "hip-environment"

    protected def requiredHeaders: Seq[(String, String)] = List(
      "Authorization"        -> s"Basic $token",
      "Environment"          -> environment,
      "User-Agent"           -> "this-api",
      "CorrelationId"        -> correlationId,
      "Gov-Test-Scenario"    -> "DEFAULT"
    ) ++ intent.map("intent" -> _)

    MockedAppConfig.hipDownstreamConfig
      .anyNumberOfTimes() returns BasicAuthDownstreamConfig(this.baseUrl, environment, clientId, clientSecret, Some(allowedHeaders))

  }

}
