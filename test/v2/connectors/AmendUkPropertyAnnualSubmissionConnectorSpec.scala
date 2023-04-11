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
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v2.models.request.amendUkPropertyAnnualSubmission._

import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"

  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "AmendUkPropertyAnnualSubmissionConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))

    "amendUkPropertyAnnualSubmissionConnector" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }

    "amendUkPropertyAnnualSubmissionConnector called for a Tax Year Specific tax year" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }

    "response is an error" must {

      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new TysIfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: AmendUkPropertyAnnualSubmissionConnector = new AmendUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    private val requestBody: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: AmendUkPropertyAnnualSubmissionRequest = AmendUkPropertyAnnualSubmissionRequest(
      nino = Nino(nino),
      businessId = businessId,
      taxYear = taxYear,
      body = requestBody
    )

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual?" +
          s"taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=${taxYear.asMtd}",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
