/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.deletePropertyAnnualSubmission

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import uk.gov.hmrc.http.StringContextOps
import v6.deletePropertyAnnualSubmission.model.request.{Def1_DeletePropertyAnnualSubmissionRequestData, DeletePropertyAnnualSubmissionRequestData}

import scala.concurrent.Future

class DeletePropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val preTysTaxYear  = TaxYear.fromMtd("2021-22")
  private val tysTaxYear2324 = TaxYear.fromMtd("2023-24")
  private val tysTaxYear2526 = TaxYear.fromMtd("2025-26")

  "DeletePropertyAnnualSubmissionConnector" must {
    "return a NO_CONTENT response" when {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      "a nonTys deletion is made" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))

        result shouldBe outcome
      }

      "a TYS 2023-24 tax year deletion is made" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear2324

        stubIfsTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a TYS 2025-26 tax year deletion is made and hip migration feature switch is disabled" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear2526
        MockedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1863.enabled" -> false))
        stubIfsTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a TYS 2025-26 tax year deletion is made and hip migration feature switch is enabled" in new HipTest with Test {
        def taxYear: TaxYear = tysTaxYear2526

        MockedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1863.enabled" -> true))
        stubHipTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }

    "return the downstream error response" when {
      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "a nonTys deletion is made" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear
        MockedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1863.enabled" -> false))
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a TYS 2023-24 tax year deletion is made" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear2324

        stubIfsTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a TYS 2025-26 tax year deletion is made and hip migration feature switch is disabled" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear2526
        MockedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1863.enabled" -> false))
        stubIfsTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a TYS 2025-26 tax year deletion is made and hip migration feature switch is enabled" in new HipTest with Test {
        def taxYear: TaxYear = tysTaxYear2526
        MockedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1863.enabled" -> true))
        stubHipTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected def taxYear: TaxYear

    val connector: DeletePropertyAnnualSubmissionConnector = new DeletePropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: DeletePropertyAnnualSubmissionRequestData =
      Def1_DeletePropertyAnnualSubmissionRequestData(nino = nino, businessId = businessId, taxYear = taxYear)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): Unit =
      willDelete(
        url = url"$baseUrl/income-tax/business/property/annual?taxableEntityId=AA123456A&incomeSourceId=XAIS12345678910&taxYear=2021-22"
      ).returns(Future.successful(outcome))

    protected def stubIfsTysHttpResponse(outcome: DownstreamOutcome[Unit]): Unit =
      willDelete(
        url = url"$baseUrl/income-tax/business/property/annual/${request.taxYear.asTysDownstream}/${request.nino}/${request.businessId}"
      ).returns(Future.successful(outcome))

    protected def stubHipTysHttpResponse(outcome: DownstreamOutcome[Unit]): Unit =
      willDelete(
        url = url"$baseUrl/itsa/income-tax/v1/${request.taxYear.asTysDownstream}/business/property/annual/${request.nino}/${request.businessId}"
      ).returns(Future.successful(outcome))

  }

}
