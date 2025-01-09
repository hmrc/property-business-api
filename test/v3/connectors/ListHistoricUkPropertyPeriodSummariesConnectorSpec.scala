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

import common.models.domain.HistoricPropertyType
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import v3.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData
import v3.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  "connector" must {
    "send a request and return a body for FHL" in new IfsTest with Test {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]] =
        await(connector.listPeriodSummaries(request, HistoricPropertyType.Fhl))
      result shouldBe outcome
    }

    "send a request and return a body for non-FHL" in new IfsTest with Test {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]] =
        await(connector.listPeriodSummaries(request, HistoricPropertyType.NonFhl))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: ListHistoricUkPropertyPeriodSummariesConnector = new ListHistoricUkPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: ListHistoricUkPropertyPeriodSummariesRequestData =
      ListHistoricUkPropertyPeriodSummariesRequestData(nino)

    private val response =
      ListHistoricUkPropertyPeriodSummariesResponse(List(SubmissionPeriod("2020-06-22", "2020-06-22")))

    protected val outcome: Right[Nothing, ResponseWrapper[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]] = Right(
      ResponseWrapper(correlationId, response))

  }

}
