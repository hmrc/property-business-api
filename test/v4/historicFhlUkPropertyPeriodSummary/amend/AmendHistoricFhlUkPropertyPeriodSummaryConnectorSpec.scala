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

package v4.historicFhlUkPropertyPeriodSummary.amend

import common.models.domain.PeriodId
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.historicFhlUkPropertyPeriodSummary.amend.request.{
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData
}

import scala.concurrent.Future

class AmendHistoricFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA123456A"
  private val periodId     = PeriodId(from = "2017-04-06", to = "2017-07-04")

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url =
          url"$baseUrl/income-tax/nino/${request.nino.value}/uk-properties/furnished-holiday-lettings/periodic-summaries?from=${request.periodId.from}&to=${request.periodId.to}",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Unit] = await(connector.amend(request))
      result shouldBe outcome
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected val connector = new AmendHistoricFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val requestBody: Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody =
      Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody(None, None)

    protected val request: Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData(Nino(nino), periodId, requestBody)

  }

}
