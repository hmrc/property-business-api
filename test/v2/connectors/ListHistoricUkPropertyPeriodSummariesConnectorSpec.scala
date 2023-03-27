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

import api.connectors.ConnectorSpec
import v2.models.domain.HistoricPropertyType
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequest
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val request: ListHistoricUkPropertyPeriodSummariesRequest = ListHistoricUkPropertyPeriodSummariesRequest(
    nino = Nino(nino)
  )

  private val response = ListHistoricUkPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod("2020-06-22", "2020-06-22")
    ))

  trait Test {
    _: ConnectorTest =>

    val connector: ListHistoricUkPropertyPeriodSummariesConnector = new ListHistoricUkPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    val outcome = Right(ResponseWrapper(correlationId, response))

    "send a request and return a body for FHL" in new IfsTest with Test {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries"
      ).returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(request, HistoricPropertyType.Fhl)) shouldBe outcome
    }

    "send a request and return a body for non-FHL" in new IfsTest with Test {
      willGet(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries"
      ).returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(request, HistoricPropertyType.NonFhl)) shouldBe outcome
    }
  }
}
