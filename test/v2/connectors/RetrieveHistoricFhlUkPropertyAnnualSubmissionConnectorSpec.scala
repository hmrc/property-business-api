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
import org.scalamock.handlers.CallHandler
import v2.mocks.MockHttpClient
import v2.models.domain.Nino
import v2.models.errors.{ DownstreamErrorCode, DownstreamErrors }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.{
  AnnualAdjustments,
  AnnualAllowances,
  RentARoom,
  RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse
}

import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA123456A"
  val taxYear: String = "2020"

  val request: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest = RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  val annualAdjustments = AnnualAdjustments(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("200.11")),
    Option(BigDecimal("105.11")),
    true,
    Option(BigDecimal("100.11")),
    false,
    Option(RentARoom(true))
  )

  val annualAllowances = AnnualAllowances(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("300.11")),
    Option(BigDecimal("405.11")),
    Option(BigDecimal("550.11"))
  )

  def responseWith(annualAdjustments: Option[AnnualAdjustments],
                   annualAllowances: Option[AnnualAllowances]): RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse =
    RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(annualAdjustments, annualAllowances)

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector = new RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDownstreamHeaders)

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]]]#Derived = {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$taxYear",
          config = dummyDesHeaderCarrierConfig,
          requiredHeaders = requiredDesHeaders,
        )
        .returns(Future.successful(outcome))
    }
  }

  "connector" when {
    "request asks for a historic FHL UK property annual submission" must {
      "return a valid result" in new Test {
        val response = responseWith(Some(annualAdjustments), Some(annualAllowances))
        val outcome  = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }

      "return an error" in new Test {
        val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

  }
}
