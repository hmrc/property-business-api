/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.updateForeignPropertyDetails

import common.models.domain.PropertyId
import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.updateForeignPropertyDetails.def1.model.request.{Def1_UpdateForeignPropertyDetailsRequestBody, Def1_UpdateForeignPropertyDetailsRequestData}
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData

import scala.concurrent.Future

class UpdateForeignPropertyDetailsConnectorSpec extends ConnectorSpec {

  private val nino: String       = "AA999999A"
  private val propertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  private val outcome = Right(ResponseWrapper(correlationId, ()))

  "UpdateForeignPropertyDetailsConnector" when {
    "sending a request which results in a 204 response" must {
      "return the expected result" in new HipTest with Test {

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.update(request))
        result shouldBe outcome
      }
    }

    "the response is an error" must {

      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new HipTest with Test {

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.update(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    val taxYear: TaxYear = TaxYear.fromMtd("2026-27")

    protected val connector: UpdateForeignPropertyDetailsConnector = new UpdateForeignPropertyDetailsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    private val requestBody: Def1_UpdateForeignPropertyDetailsRequestBody =
      Def1_UpdateForeignPropertyDetailsRequestBody("Bob & Bobby Co", Some("2026-08-24"), Some("added-in-error"))

    protected val request: UpdateForeignPropertyDetailsRequestData =
      Def1_UpdateForeignPropertyDetailsRequestData(Nino(nino), PropertyId(propertyId), taxYear, requestBody)

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = url"$baseUrl/itsd/income-sources/$nino/foreign-property-details/$propertyId?taxYear=${taxYear.asTysDownstream}",
        body = requestBody
      ).returns(Future.successful(outcome))
    }

  }

}
