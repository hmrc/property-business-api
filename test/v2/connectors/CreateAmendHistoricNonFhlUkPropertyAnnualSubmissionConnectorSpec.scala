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
import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.RequestResponseModelFixtures
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.ResponseWrapper
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with RequestResponseModelFixtures {

  val nino: String              = "AA123456A"
  val taxYear: String           = "2019-20"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  val request: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(
    Nino(nino),
    TaxYear.fromMtd(taxYear),
    requestBody
  )

  trait Test {
    _: ConnectorTest =>

    val connector = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val outcome = Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))
  }

  "connector" must {

    "put a non-fhl body and return a 200" in new IfsTest with Test {

      willPut(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear",
        body = requestBody
      ).returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome

    }
  }
}
