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

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class RetrieveForeignPropertyConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request = RetrieveForeignPropertyRequest(nino, businessId, submissionId)

  val response = RetrieveForeignPropertyResponse()

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveForeignPropertyConnector = new RetrieveForeignPropertyConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "connector" must {
    "put a body and return 204 no body" in new Test {

      val outcome = Right(ResponseWrapper(correlationId, response))
      MockedHttpClient
        .get(
          url = s"$baseUrl/business/property/${nino}/${businessId}/period/${submissionId}",
          requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.retrieveForeignProperty(request)) shouldBe outcome

    }
  }
}
