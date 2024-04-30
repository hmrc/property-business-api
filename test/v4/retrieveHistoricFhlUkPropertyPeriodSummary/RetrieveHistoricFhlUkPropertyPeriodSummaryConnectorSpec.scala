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

package v4.retrieveHistoricFhlUkPropertyPeriodSummary

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, PeriodId}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.request.{Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData, RetrieveHistoricFhlUkPiePeriodSummaryRequestData}
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.response.{Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse, RetrieveHistoricFhlUkPiePeriodSummaryResponse}

import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")
  private val periodIdFrom = "2017-04-06"
  private val periodIdTo = "2017-07-04"
  private val periodId = PeriodId(periodIdFrom, periodIdTo)

  "retrieve" should {
    "return a valid response" when {
      "a valid request is supplied" in new DesTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHistoricFhlUkPiePeriodSummaryResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return an error as per the spec" when {
      "an error response received" in new DesTest with Test {
        private val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveHistoricFhlUkPropertyPeriodSummaryConnector =
      new RetrieveHistoricFhlUkPropertyPeriodSummaryConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse])
    : CallHandler[Future[DownstreamOutcome[RetrieveHistoricFhlUkPiePeriodSummaryResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summary-detail?from=$periodIdFrom&to=$periodIdTo"
      ).returns(Future.successful(outcome))
    }

    protected val request: RetrieveHistoricFhlUkPiePeriodSummaryRequestData =
      Def1_RetrieveHistoricFhlUkPiePeriodSummaryRequestData(nino, periodId)

    protected val response: RetrieveHistoricFhlUkPiePeriodSummaryResponse =
      Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse("startDate", "toDate", None, None)

  }

}
