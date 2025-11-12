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

package v6.createForeignPropertyDetails

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.*
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.createForeignPropertyDetails.def1.model.Def1_CreateForeignPropertyDetailsFixtures
import v6.createForeignPropertyDetails.def1.model.request.{Def1_CreateForeignPropertyDetailsRequestBody, Def1_CreateForeignPropertyDetailsRequestData}
import v6.createForeignPropertyDetails.def1.model.response.Def1_CreateForeignPropertyDetailsResponse
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData
import v6.createForeignPropertyDetails.model.response.CreateForeignPropertyDetailsResponse

import scala.concurrent.Future

class CreateForeignPropertyDetailsConnectorSpec extends ConnectorSpec with Def1_CreateForeignPropertyDetailsFixtures {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val tysTaxYear = TaxYear.fromMtd("2026-27")

  "connector" must {

    "put a valid body and return 204 for a valid tax year" in new HipTest with Test {
      def taxYear: TaxYear             = tysTaxYear
      val tysDownstreamTaxYear: String = tysTaxYear.asTysDownstream

      val outcome: DownstreamOutcome[CreateForeignPropertyDetailsResponse] = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = url"$baseUrl/itsd/income-sources/$nino/foreign-property-details/$businessId?taxYear=$tysDownstreamTaxYear",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateForeignPropertyDetailsResponse] = await(connector.createForeignPropertyDetails(request))
      result shouldBe outcome
    }
  }

  trait Test {
    self: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: CreateForeignPropertyDetailsConnector =
      new CreateForeignPropertyDetailsConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val requestBody: Def1_CreateForeignPropertyDetailsRequestBody = def1_CreateForeignPropertyDetailsModel

    protected val request: CreateForeignPropertyDetailsRequestData =
      Def1_CreateForeignPropertyDetailsRequestData(nino, businessId, taxYear, requestBody)

    protected val response: Def1_CreateForeignPropertyDetailsResponse = Def1_CreateForeignPropertyDetailsResponse(
      "8e8b8450-dc1b-4360-8109-7067337b42cb"
    )

  }

}
