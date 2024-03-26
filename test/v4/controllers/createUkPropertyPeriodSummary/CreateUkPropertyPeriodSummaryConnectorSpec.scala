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

package v4.controllers.createUkPropertyPeriodSummary

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v4.controllers.createUkPropertyPeriodSummary.model.request._
import v4.controllers.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  "connector" must {
    "post a body and return 200 with submissionId" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2022-23")

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2022-23&incomeSourceId=$businessId",
        body = requestBody
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse] = await(connector.createUkProperty(requestData))
      result shouldBe outcome
    }

    "post a body and return 200 with submissionId for TYS" in new TysIfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = requestBody
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse] = await(connector.createUkProperty(requestData))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val requestBody: Def1_CreateUkPropertyPeriodSummaryRequestBody =
      Def1_CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

    protected val requestData: CreateUkPropertyPeriodSummaryRequestData =
      Def1_CreateUkPropertyPeriodSummaryRequestData(nino, taxYear, businessId, requestBody)

    protected val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
    protected val outcome: Right[Nothing, ResponseWrapper[CreateUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

    protected val connector: CreateUkPropertyPeriodSummaryConnector = new CreateUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
