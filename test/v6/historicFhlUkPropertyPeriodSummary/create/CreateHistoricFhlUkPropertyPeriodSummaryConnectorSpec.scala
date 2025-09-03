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

package v6.historicFhlUkPropertyPeriodSummary.create

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.historicFhlUkPropertyPeriodSummary.create.model.request.{
  Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody,
  Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData
}

import scala.concurrent.Future

class CreateHistoricFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA123456A"

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, ()))

      willPost(
        url = url"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Unit] = await(connector.create(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateHistoricFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val requestBody: Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody("startDate", "fromDate", None, None)

    protected val request: Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData(Nino(nino), requestBody)

  }

}
