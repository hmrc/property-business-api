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
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequest
import v2.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.Future

class ListPropertyPeriodSummariesConnectorSpec extends ConnectorSpec {

  val nino: String        = "AA123456A"
  val businessId: String  = "XAIS12345678910"
  val taxYear2023: String = "2022-23"
  val taxYear2024: String = "2023-24"

  def makeRequest(taxYear: String): ListPropertyPeriodSummariesRequest = ListPropertyPeriodSummariesRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = TaxYear.fromMtd(taxYear)
  )

  val nonTysRequest = makeRequest(taxYear2023)
  val tysRequest    = makeRequest(taxYear2024)

  private val response = ListPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")
    ))

  trait Test {
    _: ConnectorTest =>

    val connector: ListPropertyPeriodSummariesConnector = new ListPropertyPeriodSummariesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "send a request and return a body for a non-tys tax year" in new IfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = s"$baseUrl/income-tax/business/property/$nino/$businessId/period",
        parameters = Seq("taxYear" -> "2022-23")
      ).returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(nonTysRequest)) shouldBe outcome
    }

    "send a request and return a body for a tys tax year" in new TysIfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = s"$baseUrl/income-tax/business/property/23-24/$nino/$businessId/period",
      ).returns(Future.successful(outcome))

      await(connector.listPeriodSummaries(tysRequest)) shouldBe outcome
    }
  }
}
