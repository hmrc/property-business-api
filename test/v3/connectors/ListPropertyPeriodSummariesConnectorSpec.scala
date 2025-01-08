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

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v3.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequestData
import v3.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.Future

class ListPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  private val nino          = Nino("AA123456A")
  private val businessId    = BusinessId("XAIS12345678910")
  private val preTysTaxYear = "2022-23"
  private val tysTaxYear    = "2023-24"

  "connector" must {
    "send a request and return a body for a non-tys tax year" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd(preTysTaxYear)

      val outcome: Right[Nothing, ResponseWrapper[ListPropertyPeriodSummariesResponse]] = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = s"$baseUrl/income-tax/business/property/$nino/$businessId/period",
        parameters = List("taxYear" -> "2022-23")
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListPropertyPeriodSummariesResponse] = await(connector.listPeriodSummaries(request))
      result shouldBe outcome
    }

    "send a request and return a body for a tys tax year" in new TysIfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: Right[Nothing, ResponseWrapper[ListPropertyPeriodSummariesResponse]] = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = s"$baseUrl/income-tax/business/property/23-24/$nino/$businessId/period"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListPropertyPeriodSummariesResponse] = await(connector.listPeriodSummaries(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val connector: ListPropertyPeriodSummariesConnector = new ListPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: ListPropertyPeriodSummariesRequestData = ListPropertyPeriodSummariesRequestData(nino, businessId, taxYear)

    protected val response: ListPropertyPeriodSummariesResponse = ListPropertyPeriodSummariesResponse(
      List(SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")))

  }

}
