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

package v6.historicNonFhlUkPropertyPeriodSummary.list

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.historicNonFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v6.historicNonFhlUkPropertyPeriodSummary.list.model.request.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData, ListHistoricNonFhlUkPropertyPeriodSummariesRequestData}
import v6.historicNonFhlUkPropertyPeriodSummary.list.model.response.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse, ListHistoricNonFhlUkPropertyPeriodSummariesResponse}

import scala.concurrent.Future

class ListHistoricNonFhlUkPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  "connector" should {

    "send a request and return the response body" in new IfsTest with Test {
      willGet(
        url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListHistoricNonFhlUkPropertyPeriodSummariesResponse] =
        await(connector.listPeriodSummaries(request))

      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: ListHistoricNonFhlUkPropertyPeriodSummariesConnector =
      new ListHistoricNonFhlUkPropertyPeriodSummariesConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val request: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData(nino)

    private val response =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(List(SubmissionPeriod("2020-06-22", "2020-06-22")))

    protected val outcome: Right[Nothing, ResponseWrapper[ListHistoricNonFhlUkPropertyPeriodSummariesResponse]] =
      Right(
        ResponseWrapper(correlationId, response)
      )

  }

}
