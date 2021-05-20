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

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRequest
import v1.models.response.listForeignPropertiesPeriodSummaries._

import scala.concurrent.Future

class ListForeignPropertiesPeriodSummariesConnectorSpec extends ConnectorSpec {

  val nino: Nino = Nino("AA123456A")
  val businessId: String = "XAIS12345678910"
  val fromDate: String = "2020-06-01"
  val toDate: String = "2020-08-31"

  val request: ListForeignPropertiesPeriodSummariesRequest = ListForeignPropertiesPeriodSummariesRequest(
    nino = nino,
    businessId = businessId,
    fromDate = fromDate,
    toDate = toDate
  )

  private val response = ListForeignPropertiesPeriodSummariesResponse(Seq(
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
  ))

  class Test extends MockHttpClient with MockAppConfig {
    val connector: ListForeignPropertiesPeriodSummariesConnector = new ListForeignPropertiesPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
  }

  "connector" must {
    "send a request and return a body" in new Test {

      val outcome = Right(ResponseWrapper(correlationId, response))
      MockedHttpClient
        .get(
          url = s"$baseUrl/income-tax/business/property/$nino/$businessId/period?fromDate=$fromDate&toDate=$toDate",
          requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token"
        )
        .returns(Future.successful(outcome))

      await(connector.listForeignProperties(request)) shouldBe outcome

    }
  }
}
