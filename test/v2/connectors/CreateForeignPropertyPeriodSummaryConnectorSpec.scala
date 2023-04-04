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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import fixtures.CreateForeignPropertyPeriodSummaryFixtures.CreateForeignPropertyPeriodSummaryFixtures
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with CreateForeignPropertyPeriodSummaryFixtures {

  private val businessId: String = "XAIS12345678910"
  private val nino: String       = "AA123456A"

  private val preTysTaxYear = "2019-20"
  private val tysTaxYear    = "2023-24"

  "connector" must {
    "post a valid body and return 200 with submissionId" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(preTysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
      result shouldBe outcome

    }

    "post a valid body and return 200 with submissionId for a TYS tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: CreateForeignPropertyPeriodSummaryConnector = new CreateForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val requestBody: CreateForeignPropertyPeriodSummaryRequestBody = regularExpensesRequestBody

    protected val request: CreateForeignPropertyPeriodSummaryRequest =
      CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, requestBody)

    protected val response: CreateForeignPropertyPeriodSummaryResponse =
      CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  }

}
