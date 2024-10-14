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

package v5.createForeignPropertyPeriodCumulativeSummary

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.Def1_CreateForeignPropertyPeriodCumulativeSummaryFixtures
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.{
  Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody,
  Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData
}
import v5.createForeignPropertyPeriodCumulativeSummary.model.request._
import v5.createForeignPropertyPeriodCumulativeSummary.model.response.CreateForeignPropertyPeriodCumulativeSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodCumulativeSummaryConnectorSpec extends ConnectorSpec with Def1_CreateForeignPropertyPeriodCumulativeSummaryFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val preTysTaxYear = "2019-20"
  private val tysTaxYear    = "2023-24"

  "connector" must {
    "post a valid body and return 200 with submissionId" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(preTysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodCumulativeSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyPeriodCumulativeSummaryResponse] = await(connector.createForeignProperty(request))
      result shouldBe outcome

    }

    "post a valid body and return 200 with submissionId for a TYS tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodCumulativeSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyPeriodCumulativeSummaryResponse] = await(connector.createForeignProperty(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: CreateForeignPropertyPeriodCumulativeSummaryConnector = new CreateForeignPropertyPeriodCumulativeSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val requestBody: Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody = regularExpensesRequestBody

    protected val request: CreateForeignPropertyPeriodCumulativeSummaryRequestData =
      Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestData(nino, businessId, taxYear, requestBody)

    protected val response: CreateForeignPropertyPeriodCumulativeSummaryResponse =
      CreateForeignPropertyPeriodCumulativeSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  }

}
