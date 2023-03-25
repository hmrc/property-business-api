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

package v2.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import org.scalamock.handlers.CallHandler
import v2.models.domain.PeriodId
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequest
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary.{PeriodExpenses, PeriodIncome, RetrieveHistoricFhlUkPiePeriodSummaryResponse}

import scala.concurrent.Future

class RetrieveHistoricFhlUKPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String         = "AA123456A"
  val periodId: String     = "2017-04-06_2017-07-04"
  val periodIdFrom: String = "2017-04-06"
  val periodIdTo: String   = "2017-07-04"

  val request: RetrieveHistoricFhlUkPiePeriodSummaryRequest =
    RetrieveHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))

  val periodExpenses: PeriodExpenses = PeriodExpenses(None, None, None, None, None, None, None, None, None)
  val periodIncome: PeriodIncome     = PeriodIncome(None, None, None)

  def responseWith(periodIncome: Option[PeriodIncome], periodExpenses: Option[PeriodExpenses]): RetrieveHistoricFhlUkPiePeriodSummaryResponse =
    RetrieveHistoricFhlUkPiePeriodSummaryResponse("2017-04-06", "2017-07-04", periodIncome, periodExpenses)

  trait Test {
    _: ConnectorTest =>

    val connector: RetrieveHistoricFhlUkPropertyPeriodSummaryConnector = new RetrieveHistoricFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summary-detail?from=$periodIdFrom&to=$periodIdTo"
      ).returns(Future.successful(outcome))
    }
  }

  "connector" when {
    "request for a historic FHL UK Property Income and Expenses Period summary" must {
      "return a valid result" in new DesTest with Test {
        val response: RetrieveHistoricFhlUkPiePeriodSummaryResponse = responseWith(Some(periodIncome), Some(periodExpenses))
        val outcome                                                 = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "response is an error" must {
      "return the error" in new DesTest with Test {
        val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }
}
