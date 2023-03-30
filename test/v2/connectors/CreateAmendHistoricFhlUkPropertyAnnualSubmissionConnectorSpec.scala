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
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission._
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  val body: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(None, None)

  val request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(mtdTaxYear),
    body = body
  )

  trait Test {
    _: ConnectorTest =>

    val connector = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))

      willPut(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$downstreamTaxYear",
        body = body
      ).returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }

}
