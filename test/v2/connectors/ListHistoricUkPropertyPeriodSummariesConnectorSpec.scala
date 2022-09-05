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

import mocks.MockAppConfig
import v2.mocks.MockHttpClient
import v2.models.domain.{ HistoricPropertyType, Nino }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequest
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod }

import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val request: ListHistoricUkPropertyPeriodSummariesRequest = ListHistoricUkPropertyPeriodSummariesRequest(
    nino = Nino(nino)
  )

  private val response = ListHistoricUkPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod("2020-06-22", "2020-06-22")
    ))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListHistoricUkPropertyPeriodSummariesConnector = new ListHistoricUkPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    val outcome = Right(ResponseWrapper(correlationId, response))

    "send a request and return a body for FHL" in new Test {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries",
          config = dummyIfsHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(request, HistoricPropertyType.Fhl)) shouldBe outcome
    }

    "send a request and return a body for non-FHL" in new Test {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries",
          config = dummyIfsHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(request, HistoricPropertyType.NonFhl)) shouldBe outcome
    }
  }
}