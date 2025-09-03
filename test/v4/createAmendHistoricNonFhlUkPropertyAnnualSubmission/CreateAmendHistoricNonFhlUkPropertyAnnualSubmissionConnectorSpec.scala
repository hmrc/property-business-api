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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: String              = "AA123456A"
  private val taxYear: String           = "2019-20"
  private val downstreamTaxYear: String = "2020"

  "connector" must {
    "put a non-fhl body and return a 200" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))

      willPut(
        url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear",
        body = body
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse] = await(connector.amend(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val body: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), TaxYear.fromMtd(taxYear), body)

  }

}
