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

package v4.createAmendForeignPropertyAnnualSubmission

import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_Fixtures
import v4.createAmendForeignPropertyAnnualSubmission.model.request.*

import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with Def1_Fixtures {

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

    "put a body and return a 204 for a TYS tax year" in new IfsTest with Test {
      def taxYear: TaxYear = tysTaxYear

      stubTysHttpResponse(outcome)

      val result: DownstreamOutcome[Unit] = await(connector.createAmendForeignPropertyAnnualSubmission(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector = new CreateAmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    private val requestBody: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData(Nino(nino), BusinessId(businessId), taxYear, requestBody)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = url"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=2020-21",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = url"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
