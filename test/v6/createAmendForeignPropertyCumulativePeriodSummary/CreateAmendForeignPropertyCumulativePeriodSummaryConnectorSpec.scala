/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.{
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody,
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData
}
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.model.Def2_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.model.request.{
  Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody,
  Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData
}
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.*

import scala.concurrent.Future

class CreateAmendForeignPropertyCumulativePeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val tysTaxYear = "2025-26"

  "createAmendForeignProperty" when {

    "given a valid request (TYS)" should {
      "return a success response when feature switch is disabled (IFS enabled)" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1961.enabled" -> false))

        val requestBody: Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody =
          Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures.regularExpensesRequestBody

        val request: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData =
          Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, response))

        willPut(
          url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignProperty(request))
        result shouldBe outcome
      }

      "return a success response when feature switch is enabled when the tax year is 2025-26 (HIP enabled)" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1961.enabled" -> true))

        val requestBody: Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody =
          Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures.regularExpensesRequestBody

        val request: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData =
          Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, response))

        willPut(
          url = url"$baseUrl/itsa/income-tax/v1/25-26/business/periodic/property/$nino/$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignProperty(request))
        result shouldBe outcome
      }

      "return a success response when feature switch is enabled for tax years 2026-27 onwards (HIP enabled)" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1961.enabled" -> true))

        override def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val requestBody: Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody =
          Def2_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures.regularExpensesRequestBody

        val request: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData =
          Def2_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

        val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, response))

        willPut(
          url = url"$baseUrl/itsa/income-tax/v1/26-27/business/periodic/foreign-property/$nino/$businessId",
          body = requestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignProperty(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

    protected val connector: CreateAmendForeignPropertyCumulativePeriodSummaryConnector =
      new CreateAmendForeignPropertyCumulativePeriodSummaryConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val response: Unit = ()

  }

}
