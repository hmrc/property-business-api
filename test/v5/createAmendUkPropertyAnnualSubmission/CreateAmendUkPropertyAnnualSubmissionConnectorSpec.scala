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

package v5.createAmendUkPropertyAnnualSubmission

import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.{
  Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendUkPropertyAnnualSubmissionRequestData
}
import v5.createAmendUkPropertyAnnualSubmission.model.request._

import scala.concurrent.Future

class CreateAmendUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"

  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "CreateAmendUkPropertyAnnualSubmissionConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))

    "createAmendUkPropertyAnnualSubmissionConnector" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.createAmendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }

    "amendUkPropertyAnnualSubmissionConnector called for a Tax Year Specific tax year" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.createAmendUkPropertyAnnualSubmission(request))
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
          await(connector.createAmendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.createAmendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: CreateAmendUkPropertyAnnualSubmissionConnector = new CreateAmendUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    private val requestBody: Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody = Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendUkPropertyAnnualSubmissionRequestData = Def1_CreateAmendUkPropertyAnnualSubmissionRequestData(
      nino = Nino(nino),
      businessId = BusinessId(businessId),
      taxYear = taxYear,
      body = requestBody
    )

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = url"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=${taxYear.asMtd}",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
