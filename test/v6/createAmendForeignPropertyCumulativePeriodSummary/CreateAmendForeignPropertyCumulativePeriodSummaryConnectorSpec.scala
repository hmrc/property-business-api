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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.{
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody,
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData
}
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request._

import scala.concurrent.Future

class CreateAmendForeignPropertyCumulativePeriodSummaryConnectorSpec
    extends ConnectorSpec
    with Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val tysTaxYear = "2025-26"

  "connector" must {

    "put a valid body and return 204 for a valid tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, response))

      willPut(
        url = s"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignProperty(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: CreateAmendForeignPropertyCumulativePeriodSummaryConnector =
      new CreateAmendForeignPropertyCumulativePeriodSummaryConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val requestBody: Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody = regularExpensesRequestBody

    protected val request: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData =
      Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(nino, businessId, taxYear, requestBody)

    protected val response: Unit = ()

  }

}
