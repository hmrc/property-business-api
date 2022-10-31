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

import mocks.{MockAppConfig, MockHttpClient}
import org.scalamock.handlers.CallHandler
import v2.models.domain.{ Nino, PeriodId }
import v2.models.errors.{ DownstreamErrorCode, DownstreamErrors }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.{ RetrieveHistoricNonFhlUkPiePeriodSummaryResponse, PeriodExpenses, PeriodIncome }

import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String         = "AA123456A"
  val periodIdFrom: String = "2017-04-06"
  val periodIdTo: String   = "2017-07-04"
  val periodId: String     = s"${periodIdFrom}_$periodIdTo"

  val request: RetrieveHistoricNonFhlUkPiePeriodSummaryRequest =
    RetrieveHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))

  val periodExpenses: PeriodExpenses = PeriodExpenses(None, None, None, None, None, None, None, None, None, None, None)
  val periodIncome: PeriodIncome     = PeriodIncome(None, None, None, None, None, None)

  def responseWith(periodIncome: Option[PeriodIncome], periodExpenses: Option[PeriodExpenses]): RetrieveHistoricNonFhlUkPiePeriodSummaryResponse =
    RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(periodIdFrom, periodIdTo, periodIncome, periodExpenses)

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]]]#Derived = {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summary-detail?from=$periodIdFrom&to=$periodIdTo",
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredDesHeaders,
        )
        .returns(Future.successful(outcome))
    }
  }

  "connector" when {
    "request for a historic Non-FHL UK Property Income and Expenses Period summary" must {
      "return a valid result" in new Test {
        val response: RetrieveHistoricNonFhlUkPiePeriodSummaryResponse = responseWith(Some(periodIncome), Some(periodExpenses))
        val outcome                                                    = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "response is an error" must {
      "return the error" in new Test {
        val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }
}
