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
import v1.mocks.MockHttpClient
import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteForeignPropertyAnnualSubmission.DeleteForeignPropertyAnnualSubmissionRequest

import scala.concurrent.Future

class DeleteForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2021-22"

  val request: DeleteForeignPropertyAnnualSubmissionRequest = DeleteForeignPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: DeleteForeignPropertyAnnualSubmissionConnector = new DeleteForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    "send a request and return no content" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockHttpClient
        .delete(
          url = s"$baseUrl/income-tax/business/property/annual/$nino/$businessId/$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.deleteForeignPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}