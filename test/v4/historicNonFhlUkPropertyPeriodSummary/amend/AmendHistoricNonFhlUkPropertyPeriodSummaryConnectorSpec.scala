/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.historicNonFhlUkPropertyPeriodSummary.amend

import common.models.domain.PeriodId
import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.historicNonFhlUkPropertyPeriodSummary.amend.model.request.{
  AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData
}
import v4.historicNonFhlUkPropertyPeriodSummary.amend.model.response.AmendHistoricNonFhlUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class AmendHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino     = Nino("AA123456A")
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  "The connector" when {
    "sending a valid amend request" should {
      "return the ok result" in new IfsTest with Test {
        val response: AmendHistoricNonFhlUkPropertyPeriodSummaryResponse =
          AmendHistoricNonFhlUkPropertyPeriodSummaryResponse(transactionReference = "2017090920170909")

        val outcome: Right[Nothing, ResponseWrapper[AmendHistoricNonFhlUkPropertyPeriodSummaryResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amend(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected val connector: AmendHistoricNonFhlUkPropertyPeriodSummaryConnector = new AmendHistoricNonFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    private val requestBody: Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(None, None)

    protected val request: AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData(nino, periodId, requestBody)

    def stubHttpResponse(outcome: DownstreamOutcome[AmendHistoricNonFhlUkPropertyPeriodSummaryResponse])
        : CallHandler[Future[DownstreamOutcome[AmendHistoricNonFhlUkPropertyPeriodSummaryResponse]]]#Derived = {

      willPut(
        url =
          url"$baseUrl/income-tax/nino/${request.nino.value}/uk-properties/other/periodic-summaries?from=${request.periodId.from}&to=${request.periodId.to}",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
