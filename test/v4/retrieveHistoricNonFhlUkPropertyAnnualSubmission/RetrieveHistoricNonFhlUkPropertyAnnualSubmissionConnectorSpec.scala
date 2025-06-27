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

package v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.http.StringContextOps
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.{Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData}
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.response.{Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse}

import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino              = Nino("AA123456A")
  private val taxYear           = TaxYear.fromMtd("2019-20")
  private val downstreamTaxYear = "2020"

  "retrieve" should {
    "return a valid response" when {
      "a valid request is supplied" in new IfsTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return an error as per the spec" when {
      "an error response received" in new IfsTest with Test {
        private val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector =
      new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]]]#Derived = {
      willGet(
        url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear"
      ).returns(Future.successful(outcome))
    }

    protected val request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData(nino, taxYear)

    protected val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(None, None)

  }

}
