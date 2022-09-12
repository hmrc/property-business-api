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
import v2.models.domain.{ HistoricPropertyType, Nino, TaxYear }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequest

import scala.concurrent.Future

class DeleteHistoricUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val mtdTaxYear: String = "2021-22"
  val taxYear: TaxYear   = TaxYear.fromMtd(mtdTaxYear)

  def request(propertyType: HistoricPropertyType): DeleteHistoricUkPropertyAnnualSubmissionRequest =
    DeleteHistoricUkPropertyAnnualSubmissionRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      propertyType
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteHistoricUkPropertyAnnualSubmissionConnector = new DeleteHistoricUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    "send a request and return no content for FHL" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/${taxYear.toDownstream}",
          config = dummyIfsHeaderCarrierConfig,
          body = JsObject.empty,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.deleteHistoricUkPropertyAnnualSubmission(request(HistoricPropertyType.Fhl))) shouldBe outcome
    }

    "send a request and return no content for non-FHL" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/${taxYear.toDownstream}",
          config = dummyIfsHeaderCarrierConfig,
          body = JsObject.empty,
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.deleteHistoricUkPropertyAnnualSubmission(request(HistoricPropertyType.NonFhl))) shouldBe outcome
    }
  }
}