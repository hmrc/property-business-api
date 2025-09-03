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

package v5.amendUkPropertyPeriodSummary

import common.models.domain.SubmissionId
import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v5.amendUkPropertyPeriodSummary.model.request.*

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino: String         = "AA123456A"
  private val businessId: String   = "XAIS12345678910"
  private val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "AmendUkPropertyPeriodSummaryConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))

    "amendUkPropertyPeriodSummary" must {
      "send a request and return 204 no content" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyPeriodSummary(request))
        result shouldBe outcome
      }
    }

    "amendUkPropertyPeriodSummary is called with a TYS tax year" must {
      "send a request and return 204 no content" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyPeriodSummary(request))
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

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyPeriodSummary(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear
        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.amendUkPropertyPeriodSummary(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: AmendUkPropertyPeriodSummaryConnector = new AmendUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    private val requestBody: Def1_AmendUkPropertyPeriodSummaryRequestBody = Def1_AmendUkPropertyPeriodSummaryRequestBody(None, None)

    protected val request: AmendUkPropertyPeriodSummaryRequestData =
      Def1_AmendUkPropertyPeriodSummaryRequestData(Nino(nino), taxYear, BusinessId(businessId), SubmissionId(submissionId), requestBody)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url =
          url"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url =
          url"$baseUrl/income-tax/business/property/periodic/${taxYear.asTysDownstream}?taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
