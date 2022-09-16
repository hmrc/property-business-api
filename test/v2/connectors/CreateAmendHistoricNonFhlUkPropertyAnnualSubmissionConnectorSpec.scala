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

import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.RequestResponseModelsFixture
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockHttpClient
import v2.models.domain.{ Nino, TaxYear }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with RequestResponseModelsFixture {

  val nino: String              = "AA123456A"
  val taxYear: String           = "2019-20"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  val request = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(
    Nino(nino),
    TaxYear.fromMtd(taxYear),
    body
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDownstreamHeaders)

    val outcome                    = Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))
    implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

    val requiredDownstreamHeadersPut: Seq[(String, String)] =
      requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

  }

  "connector" must {

    "put a non-fhl body and return a 200" in new Test {

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredDownstreamHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome

    }
  }
}
