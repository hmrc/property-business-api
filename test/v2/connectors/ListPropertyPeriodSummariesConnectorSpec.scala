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

import mocks.MockAppConfig
import v2.mocks.MockHttpClient
import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequest
import v2.models.response.listPropertyPeriodSummaries.{ ListPropertyPeriodSummariesResponse, SubmissionPeriod }

import scala.concurrent.Future

class ListPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String    = "2022-23"

  val request: ListPropertyPeriodSummariesRequest = ListPropertyPeriodSummariesRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear
  )

  private val response = ListPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")
    ))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListPropertyPeriodSummariesConnector = new ListPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    "send a request and return a body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/business/property/$nino/$businessId/period",
          queryParams = Seq("taxYear" -> taxYear),
          config = dummyIfsHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(request)) shouldBe outcome

    }
  }
}
