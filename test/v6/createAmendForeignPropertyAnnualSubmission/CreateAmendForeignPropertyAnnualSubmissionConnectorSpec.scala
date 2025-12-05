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

package v6.createAmendForeignPropertyAnnualSubmission

import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.{
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData,
  Def1_Fixtures
}
import v6.createAmendForeignPropertyAnnualSubmission.def2.model.request.{
  Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData,
  Def2_Fixtures
}
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.{
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData,
  Def3_Fixtures
}
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with Def1_Fixtures with Def2_Fixtures with Def3_Fixtures {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"

  "CreateAmendForeignPropertyAnnualSubmissionConnector" should {
    "return a 204 response" when {

      val outcome = Right(ResponseWrapper(correlationId, ()))

      "a request is made for tax year 2021-22" in new IfsTest with Test {

        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        willPut(
          url = url"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=2021-22",
          body = createAmendForeignPropertyAnnualSubmissionRequestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a request is made for tax year 2023-24" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1804.enabled" -> true))
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPut(
          url = url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId",
          body = def2_createAmendForeignPropertyAnnualSubmissionRequestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a request is made for tax year 2025-26 (HIP disabled)" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1804.enabled" -> false))

        def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

        willPut(
          url = url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId",
          body = def2_createAmendForeignPropertyAnnualSubmissionRequestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a request is made for tax year 2025-26 (HIP enabled)" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1804.enabled" -> true))
        def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

        willPut(
          url = url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId",
          body = def2_createAmendForeignPropertyAnnualSubmissionRequestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "a request is made for tax year 2026-27" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1804.enabled" -> true))

        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        willPut(
          url = url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/foreign-property/annual/$nino/$businessId",
          body = def3_createAmendForeignPropertyAnnualSubmissionRequestBody
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector = new CreateAmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequestData = taxYear.year match {
      case ty if ty >= 2027 =>
        Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
          Nino(nino),
          BusinessId(businessId),
          taxYear,
          def3_createAmendForeignPropertyAnnualSubmissionRequestBody)
      case ty if ty >= 2024 =>
        Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData(
          Nino(nino),
          BusinessId(businessId),
          taxYear,
          def2_createAmendForeignPropertyAnnualSubmissionRequestBody)
      case _ =>
        Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData(
          Nino(nino),
          BusinessId(businessId),
          taxYear,
          createAmendForeignPropertyAnnualSubmissionRequestBody)
    }

  }

}
