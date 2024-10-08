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

package v3.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v3.models.request.createAmendForeignPropertyAnnualSubmission._

import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val nino: String       = "AA123456A"
  private val businessId: String = "XAIS12345678910"

  private val preTysTaxYear = TaxYear.fromMtd("2020-21")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "CreateAmendForeignPropertyAnnualSubmissionConnector" must {

    val outcome = Right(ResponseWrapper(correlationId, ()))

    "put a body and return a 204" in new IfsTest with Test {
      def taxYear: TaxYear = preTysTaxYear

      stubHttpResponse(outcome)

      val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
      result shouldBe outcome
    }

    "put a body and return a 204 for a TYS tax year" in new TysIfsTest with Test {
      def taxYear: TaxYear = tysTaxYear

      stubTysHttpResponse(outcome)

      val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector = new CreateAmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    private val requestBody: CreateAmendForeignPropertyAnnualSubmissionRequestBody = CreateAmendForeignPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      CreateAmendForeignPropertyAnnualSubmissionRequestData(Nino(nino), BusinessId(businessId), taxYear, requestBody)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=2020-21",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
