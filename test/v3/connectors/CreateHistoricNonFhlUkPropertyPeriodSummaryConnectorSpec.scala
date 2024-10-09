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

package v3.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v3.models.request.createHistoricNonFhlUkPropertyPeriodSummary._

import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino: String = "TC663795B"
  private val fromDate     = "2021-01-06"
  private val toDate       = "2021-02-06"

  "connector" must {
    "post a body with dates, income and expenses and return a 202 with the Period ID added" in new IfsTest with Test {
      val downstreamOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

      willPost(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries",
        body = requestBody
      ).returns(Future.successful(downstreamOutcome))

      val result: DownstreamOutcome[Unit] = await(connector.createPeriodSummary(requestData))
      result shouldBe downstreamOutcome
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: CreateHistoricNonFhlUkPropertyPeriodSummaryConnector = new CreateHistoricNonFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val requestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
      CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(fromDate, toDate, None, None)

    protected val requestData: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData(Nino(nino), requestBody)

  }

}
