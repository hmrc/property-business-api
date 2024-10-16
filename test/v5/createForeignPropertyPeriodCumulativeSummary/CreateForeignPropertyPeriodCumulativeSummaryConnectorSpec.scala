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

import scala.concurrent.Future

class CreateForeignPropertyPeriodCumulativeSummaryConnectorSpec extends ConnectorSpec with Def1_CreateForeignPropertyPeriodCumulativeSummaryFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val tysTaxYear = "2025-26"

  "connector" must {

    "post a valid body and return 204 for a valid tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd(tysTaxYear)

      val outcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, response))

      willPut(
        url = s"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Unit] = await(connector.createForeignProperty(request))
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

    protected val response: Unit = ()

  }

}
