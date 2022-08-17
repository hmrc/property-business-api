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
import play.api.libs.json.JsObject
import v2.mocks.MockHttpClient
import v2.models.domain.{ Nino, TaxYear }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.deleteHistoricFhlUkPropertyAnnualSubmission.DeleteHistoricFhlUkPropertyAnnualSubmissionRequest

import scala.concurrent.Future

class DeleteHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val mtdTaxYear: String = "2021-22"
  val taxYear: TaxYear   = TaxYear.fromMtd(mtdTaxYear)

  val request: DeleteHistoricFhlUkPropertyAnnualSubmissionRequest = DeleteHistoricFhlUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteHistoricFhlUkPropertyAnnualSubmissionConnector = new DeleteHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    "send a request and return no content" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/${taxYear.toDownstream}",
          config = dummyDesHeaderCarrierConfig,
          body = JsObject.empty,
          requiredHeaders = requiredDesHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.deleteHistoricFhlUkPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}