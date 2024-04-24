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

package v4.createAmendHistoricFhlUkPropertyAnnualSubmission

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request._
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: String              = "AA123456A"
  private val mtdTaxYear: String        = "2019-20"
  private val downstreamTaxYear: String = "2020"

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))

      willPut(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$downstreamTaxYear",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse] = await(connector.amend(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val requestBody: Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), TaxYear.fromMtd(mtdTaxYear), requestBody)

  }

}
