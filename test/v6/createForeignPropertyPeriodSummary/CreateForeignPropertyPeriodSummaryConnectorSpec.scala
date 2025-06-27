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

package v6.createForeignPropertyPeriodSummary

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.createForeignPropertyPeriodSummary.def1.model.Def1_CreateForeignPropertyPeriodSummaryFixtures
import v6.createForeignPropertyPeriodSummary.model.request._
import v6.createForeignPropertyPeriodSummary.model.response.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with Def1_CreateForeignPropertyPeriodSummaryFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val preTysTaxYear = "2019-20"
  private val tysTaxYear    = "2023-24"

  "connector" must {
    "post a valid body and return 200 with submissionId" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(preTysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = url"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
      result shouldBe outcome

    }

    "post a valid body and return 200 with submissionId for a TYS tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = url"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
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
      appConfig = mockSharedAppConfig
    )

    protected val requestBody: Def1_CreateForeignPropertyPeriodSummaryRequestBody = regularExpensesRequestBody

    protected val request: CreateForeignPropertyPeriodSummaryRequestData =
      Def1_CreateForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

    protected val response: CreateForeignPropertyPeriodSummaryResponse =
      CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  }

}
