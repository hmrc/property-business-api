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

package v5.createUkPropertyPeriodSummary

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v5.createUkPropertyPeriodSummary.model.request._
import v5.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  "connector" must {
    "post a body and return 200 with submissionId" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2022-23")

      willPost(
        url = url"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2022-23&incomeSourceId=$businessId",
        body = requestBodyDef1
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse] = await(connector.createUkProperty(requestDataDef1))
      result shouldBe outcome
    }

    "post a body and return 200 with submissionId for TYS" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

      willPost(
        url = url"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = requestBodyDef1
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse] = await(connector.createUkProperty(requestDataDef1))
      result shouldBe outcome
    }

    "post a body and return 200 with submissionId for TY24-25" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2024-25")

      willPost(
        url = url"$baseUrl/income-tax/business/property/periodic/24-25?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = requestBodyDef2
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateUkPropertyPeriodSummaryResponse] = await(connector.createUkProperty(requestDataDef2))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val requestBodyDef1: Def1_CreateUkPropertyPeriodSummaryRequestBody =
      Def1_CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

    protected val requestBodyDef2: Def2_CreateUkPropertyPeriodSummaryRequestBody =
      Def2_CreateUkPropertyPeriodSummaryRequestBody("2024-04-06", "2024-07-05", None, None)

    protected val requestDataDef1: CreateUkPropertyPeriodSummaryRequestData =
      Def1_CreateUkPropertyPeriodSummaryRequestData(nino, businessId, taxYear, requestBodyDef1)

    protected val requestDataDef2: CreateUkPropertyPeriodSummaryRequestData =
      Def2_CreateUkPropertyPeriodSummaryRequestData(nino, businessId, taxYear, requestBodyDef2)

    protected val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
    protected val outcome: Right[Nothing, ResponseWrapper[CreateUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

    protected val connector: CreateUkPropertyPeriodSummaryConnector = new CreateUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

}
