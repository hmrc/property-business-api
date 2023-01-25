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

import v2.models.domain.{ Nino, TaxYear }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String       = "AA123456A"

  val body: CreateUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

  trait Test {
    _: ConnectorTest =>

    val taxYear: TaxYear

    val requestData: CreateUkPropertyPeriodSummaryRequest = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, body)

    val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
    val outcome                                         = Right(ResponseWrapper(correlationId, response))

    val connector: CreateUkPropertyPeriodSummaryConnector = new CreateUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "post a body and return 200 with submissionId" in new IfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2022-23")

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2022-23&incomeSourceId=$businessId",
        body = body
      ) returns Future.successful(outcome)

      await(connector.createUkProperty(requestData)) shouldBe outcome
    }

    "post a body and return 200 with submissionId for TYS" in new TysIfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = body
      ) returns Future.successful(outcome)

      await(connector.createUkProperty(requestData)) shouldBe outcome
    }
  }
}
