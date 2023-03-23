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
import api.models.ResponseWrapper
import api.models.domain.Nino
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.{AmendHistoricNonFhlUkPiePeriodSummaryRequest, AmendHistoricNonFhlUkPiePeriodSummaryRequestBody}
import v2.models.response.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryResponse

import scala.concurrent.Future

class AmendHistoricNonFhlUkPiePeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  private val requestBody = AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(None, None)
  private val request     = AmendHistoricNonFhlUkPiePeriodSummaryRequest(nino, periodId, requestBody)

  "The connector" when {
    "sending a valid amend request" should {
      "return the ok result" in new IfsTest with Test {
        val response = AmendHistoricNonFhlUkPiePeriodSummaryResponse(transactionReference = "2017090920170909")
        val outcome  = Right(ResponseWrapper(correlationId, response))
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amend(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    val connector: AmendHistoricNonFhlUkPiePeriodSummaryConnector = new AmendHistoricNonFhlUkPiePeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    def pathFrom(request: AmendHistoricNonFhlUkPiePeriodSummaryRequest): String =
      s"income-tax/nino/${request.nino.value}/uk-properties/other/periodic-summaries" +
        s"?from=${request.periodId.from}" +
        s"&to=${request.periodId.to}"

    def stubHttpResponse(outcome: DownstreamOutcome[AmendHistoricNonFhlUkPiePeriodSummaryResponse])
      : CallHandler[Future[DownstreamOutcome[AmendHistoricNonFhlUkPiePeriodSummaryResponse]]]#Derived = {

      val path = pathFrom(request)

      willPut(
        url = s"$baseUrl/$path",
        body = requestBody
      ).returns(Future.successful(outcome))
    }
  }

}
