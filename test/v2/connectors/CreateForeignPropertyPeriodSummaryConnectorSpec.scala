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

import api.connectors.ConnectorSpec
import fixtures.CreateForeignPropertyPeriodSummaryFixtures.CreateForeignPropertyPeriodSummaryFixtures
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.ResponseWrapper
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with CreateForeignPropertyPeriodSummaryFixtures {

  val businessId: String = "XAIS12345678910"
  val nino: String       = "AA123456A"

  private val response = CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private def makeRequest(taxYear: String) =
    CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), regularExpensesRequestBody)

  val nonTysRequest = makeRequest("2019-20")
  val tysRequest    = makeRequest("2023-24")

  trait Test {
    _: ConnectorTest =>

    val connector: CreateForeignPropertyPeriodSummaryConnector = new CreateForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "post a valid body and return 200 with submissionId" in new IfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId",
        body = regularExpensesRequestBody
      ).returns(Future.successful(outcome))

      await(connector.createForeignProperty(nonTysRequest)) shouldBe outcome

    }

    "post a valid body and return 200 with submissionId for a TYS tax year" in new TysIfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      willPost(
        url = s"$baseUrl/income-tax/business/property/periodic/23-24?taxableEntityId=$nino&incomeSourceId=$businessId",
        body = regularExpensesRequestBody
      ).returns(Future.successful(outcome))

      await(connector.createForeignProperty(tysRequest)) shouldBe outcome

    }

  }
}
