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

import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.createForeignPropertyPeriodSummary.def1.model.Def1_CreateForeignPropertyPeriodSummaryFixtures
import v6.createForeignPropertyPeriodSummary.model.request.*
import v6.createForeignPropertyPeriodSummary.model.response.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with Def1_CreateForeignPropertyPeriodSummaryFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val preTysTaxYear = "2019-20"
  private val tysTaxYear    = "2023-24"

  "connector" must {
    "post a valid body and return 200 with submissionId for Def1" when {
      "given a preTys tax year 2019-20" in new IfsTest with Def1Test {
        def taxYear: TaxYear                                                       = TaxYear.fromMtd(preTysTaxYear)
        val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))

        willPost(
          url = url"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
        result shouldBe outcome
      }

      "given a tax year 2023-24 and HIP is disabled" in new IfsTest with Def1Test {
        def taxYear: TaxYear                                                       = TaxYear.fromMtd(tysTaxYear)
        val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1861.enabled" -> false))

        willPost(
          url = url"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
        result shouldBe outcome
      }

      "given a tax year 2023-24 and HIP is enabled" in new HipTest with Def1Test {
        def taxYear: TaxYear                                                       = TaxYear.fromMtd(tysTaxYear)
        val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1861.enabled" -> true))

        willPost(
          url = url"$baseUrl/itsa/income-tax/v1/23-24/business/property/periodic/$nino/$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
        result shouldBe outcome
      }
    }

    "post a valid body and return 200 with submissionId for Def2" when {
      "given a tax year 2023-24 and HIP is enabled" in new HipTest with Def2Test {
        def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

        val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1861.enabled" -> true))

        willPost(
          url = url"$baseUrl/itsa/income-tax/v1/23-24/business/property/periodic/$nino/$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
        result shouldBe outcome
      }

      "given a tax year 2023-24 and HIP is disabled" in new IfsTest with Def2Test {
        def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

        val outcome: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = Right(ResponseWrapper(correlationId, response))
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1861.enabled" -> false))

        willPost(
          url = url"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[CreateForeignPropertyPeriodSummaryResponse] = await(connector.createForeignProperty(request))
        result shouldBe outcome
      }
    }
  }

  trait Def1Test {
    self: ConnectorTest =>

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

  trait Def2Test {
    self: ConnectorTest =>
    
    def taxYear: TaxYear

    protected val connector: CreateForeignPropertyPeriodSummaryConnector = new CreateForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val requestBody: Def2_CreateForeignPropertyPeriodSummaryRequestBody = Def2_CreateForeignPropertyPeriodSummaryRequestBody(
      fromDate = regularExpensesRequestBody.fromDate,
      toDate = regularExpensesRequestBody.toDate,
      foreignFhlEea = None,
      foreignNonFhlProperty = None
    )

    protected val request: CreateForeignPropertyPeriodSummaryRequestData =
      Def2_CreateForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

    protected val response: CreateForeignPropertyPeriodSummaryResponse =
      CreateForeignPropertyPeriodSummaryResponse("6d553f04-9ca4-4fcf-b9f8-01b2a40340a7")

  }

}
