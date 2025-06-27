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

package v5.historicNonFhlUkPropertyPeriodSummary.retrieve

import common.models.domain.PeriodId
import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v5.historicNonFhlUkPropertyPeriodSummary.retrieve.model.request.{Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData, RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData}
import v5.historicNonFhlUkPropertyPeriodSummary.retrieve.model.response.{Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse, RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse}

import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino         = Nino("AA123456A")
  private val periodIdFrom = "2017-04-06"
  private val periodIdTo   = "2017-07-04"
  private val periodId     = PeriodId(s"${periodIdFrom}_$periodIdTo")

  "connector" when {
    "request for a historic Non-FHL UK Property Income and Expenses Period summary" must {
      "return a valid result" in new DesTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "response is an error" must {
      "return the error" in new DesTest with Test {
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse]]]#Derived = {
      willGet(
        url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summary-detail?from=$periodIdFrom&to=$periodIdTo"
      ).returns(Future.successful(outcome))
    }

    protected val request: RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData(nino, periodId)

    protected val response: RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse(periodIdFrom, periodIdTo, None, None)

  }

}
