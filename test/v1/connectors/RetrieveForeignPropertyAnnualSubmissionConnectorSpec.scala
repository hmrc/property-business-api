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
import v1.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v1.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: Nino = Nino("AA123456A")
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2019-20"

  val request: RetrieveForeignPropertyAnnualSubmissionRequest = RetrieveForeignPropertyAnnualSubmissionRequest(
    nino = nino,
    businessId = businessId,
    taxYear = taxYear
  )

  private val response = RetrieveForeignPropertyAnnualSubmissionResponse(
    Some(ForeignFhlEeaEntry(
      Some(ForeignFhlEeaAdjustments(
      Some(100.25),
      Some(100.25),
      Some(true))),
      Some(ForeignFhlEeaAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25))))),
      Some(Seq(ForeignPropertyEntry(
        "GER",
        Some(ForeignPropertyAdjustments(
          Some(100.25),
          Some(100.25))),
        Some(ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25)))))))

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveForeignPropertyAnnualSubmissionConnector = new RetrieveForeignPropertyAnnualSubmissionConnector(
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
          url = s"$baseUrl/income-tax/business/property/annual/$nino/$businessId/$taxYear",
          requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token"
        )
        .returns(Future.successful(outcome))

      await(connector.retrieveForeignProperty(request)) shouldBe outcome

    }
  }
}