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
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockHttpClient
import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyAnnualSubmission.{AmendForeignPropertyAnnualSubmissionFixture, _}

import scala.concurrent.Future

class AmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with AmendForeignPropertyAnnualSubmissionFixture {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2020-21"

  val body: AmendForeignPropertyAnnualSubmissionRequestBody = amendForeignPropertyAnnualSubmissionRequestBody

  val request: AmendForeignPropertyAnnualSubmissionRequest = AmendForeignPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear,
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new AmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    "put a body and return a 204" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amendForeignPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}
