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

package v1.connectors

import mocks.{ MockAppConfig, MockHttpClient }
import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequest
import v1.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty._

import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String         = "AA123456A"
  val businessId: String   = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request: RetrieveForeignPropertyPeriodSummaryRequest = RetrieveForeignPropertyPeriodSummaryRequest(
    nino = Nino(nino),
    businessId = businessId,
    submissionId = submissionId
  )

  private val response = RetrieveForeignPropertyPeriodSummaryResponse(
    "2020-01-01",
    "2020-01-31",
    Some(
      ForeignFhlEea(
        Some(ForeignFhlEeaIncome(Some(5000.99))),
        Some(
          ForeignFhlEeaExpenditure(
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            None
          ))
      )),
    Some(
      Seq(ForeignProperty(
        "FRA",
        ForeignPropertyIncome(
          Some(ForeignPropertyRentIncome(Some(5000.99))),
          false,
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99)
        ),
        Some(
          ForeignPropertyExpenditure(
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            None
          ))
      )))
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveForeignPropertyPeriodSummaryConnector = new RetrieveForeignPropertyPeriodSummaryConnector(
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
          url = s"$baseUrl/income-tax/business/property/periodic/$nino/$businessId/$submissionId",
          config = dummyIfsHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.retrieveForeignProperty(request)) shouldBe outcome

    }
  }
}
