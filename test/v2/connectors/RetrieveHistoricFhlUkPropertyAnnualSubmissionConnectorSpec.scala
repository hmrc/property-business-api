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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import org.scalamock.handlers.CallHandler
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: String              = "AA123456A"
  private val mtdTaxYear: String        = "2019-20"
  private val downstreamTaxYear: String = "2020"

  "connector" when {
    "request asks for a historic FHL UK property annual submission" must {
      "return a valid result" in new IfsTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }

      "response is an error" must {
        "return an error" in new IfsTest with Test {
          val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
            Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

          stubHttpResponse(outcome)

          val result: DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] = await(connector.retrieve(request))
          result shouldBe outcome
        }
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector = new RetrieveHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]]]#Derived = {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$downstreamTaxYear",
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredIfsHeaders
        )
        .returns(Future.successful(outcome))
    }

    protected val request: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest = RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(mtdTaxYear)
    )

    protected val response: RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse =
      RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(None, None)

  }

}
