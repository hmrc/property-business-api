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

package v6.deletePropertyAnnualSubmission

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v6.deletePropertyAnnualSubmission.model.request.{Def1_DeletePropertyAnnualSubmissionRequestData, DeletePropertyAnnualSubmissionRequestData}

import scala.concurrent.Future

class DeletePropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val preTysTaxYear = TaxYear.fromMtd("2021-22")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "connector" when {
    "the downstream response is a success" must {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      "return no content" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "return no content given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear
        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }

    "the downstream response is an error" must {
      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear
        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.deletePropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val taxYear: TaxYear

    val connector: DeletePropertyAnnualSubmissionConnector = new DeletePropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: DeletePropertyAnnualSubmissionRequestData =
      Def1_DeletePropertyAnnualSubmissionRequestData(nino = nino, businessId = businessId, taxYear = taxYear)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): Unit =
      willDelete(
        url = s"$baseUrl/income-tax/business/property/annual?taxableEntityId=AA123456A&incomeSourceId=XAIS12345678910&taxYear=2021-22"
      ).returns(Future.successful(outcome))

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): Unit =
      willDelete(
        url = s"$baseUrl/income-tax/business/property/annual/${request.taxYear.asTysDownstream}/${request.nino}/${request.businessId}"
      ).returns(Future.successful(outcome))

  }

}
