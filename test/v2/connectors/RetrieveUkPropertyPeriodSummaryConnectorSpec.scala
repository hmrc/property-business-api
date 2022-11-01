/*
 * Copyright 2022 HM Revenue & Customs
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

import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import org.scalamock.handlers.CallHandler
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector._
import v2.models.domain.{Nino, TaxYear}
import v2.models.errors.{DownstreamErrorCode, DownstreamErrors}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequest
import v2.models.response.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryResponse, UkFhlProperty, UkNonFhlProperty}

import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with ResponseModelsFixture {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2019-20"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request: RetrieveUkPropertyPeriodSummaryRequest =
    RetrieveUkPropertyPeriodSummaryRequest(
      Nino(nino),
      businessId,
      TaxYear.fromMtd(taxYear),
      submissionId)

  val ukFhlProperty: UkFhlProperty       = UkFhlProperty(None, None)
  val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(None, None)

  def responseWith(ukFhlProperty: Option[UkFhlProperty], ukNonFhlProperty: Option[UkNonFhlProperty]): RetrieveUkPropertyPeriodSummaryResponse =
    RetrieveUkPropertyPeriodSummaryResponse("2020-06-17T10:53:38Z", "2019-01-29", "2020-03-29", ukFhlProperty, ukNonFhlProperty)

  trait Test {
    _: ConnectorTest =>
    val connector: RetrieveUkPropertyPeriodSummaryConnector = new RetrieveUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveUkPropertyPeriodSummaryResponse])
    : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyPeriodSummaryResponse]]]#Derived = {
      willGet(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=2019-20&incomeSourceId=$businessId&submissionId=$submissionId",
        )
        .returns(Future.successful(outcome))
    }
  }

  "connector" when {
    "response has uk fhl details" must {
      "return a uk result" in new IfsTest with Test {
        val response: RetrieveUkPropertyPeriodSummaryResponse = responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new IfsTest with Test {
        val response: RetrieveUkPropertyPeriodSummaryResponse = responseWith(ukFhlProperty = None, ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new IfsTest with Test {
        val response: RetrieveUkPropertyPeriodSummaryResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has no details" must {
      "return a non-uk result" in new IfsTest with Test {
        val response: RetrieveUkPropertyPeriodSummaryResponse = responseWith(None, None)
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "response is an error" must {
      "return the error" in new IfsTest with Test {
        val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }
}
