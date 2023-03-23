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
import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import org.scalamock.handlers.CallHandler
import v2.connectors.RetrieveUkPropertyPeriodSummaryConnector._
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.ResponseWrapper
import v2.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequest
import v2.models.response.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryResponse, UkFhlProperty, UkNonFhlProperty}

import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec with ResponseModelsFixture {

  val nino: String                       = "AA123456A"
  val businessId: String                 = "XAIS12345678910"
  val submissionId: String               = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val ukFhlProperty: UkFhlProperty       = UkFhlProperty(None, None)
  val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(None, None)

  def responseWith(ukFhlProperty: Option[UkFhlProperty], ukNonFhlProperty: Option[UkNonFhlProperty]): RetrieveUkPropertyPeriodSummaryResponse =
    RetrieveUkPropertyPeriodSummaryResponse("2020-06-17T10:53:38Z", "2019-01-29", "2020-03-29", Some("2020-06-17T10:53:38Z"), ukFhlProperty, ukNonFhlProperty)

  trait Test extends ConnectorTest {

    def taxYear: String
    def uri: String

    val connector: RetrieveUkPropertyPeriodSummaryConnector = new RetrieveUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: RetrieveUkPropertyPeriodSummaryRequest =
      RetrieveUkPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId)

    def stubHttpResponse(uri: String, outcome: DownstreamOutcome[RetrieveUkPropertyPeriodSummaryResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyPeriodSummaryResponse]]]#Derived = {
      willGet(
        url = uri
      ).returns(Future.successful(outcome))
    }
  }

  trait NonTysTest extends Test with IfsTest {
    def taxYear: String = "2019-20"

    val uri: String = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=" +
      s"$nino&taxYear=2019-20&incomeSourceId=$businessId&submissionId=$submissionId"
  }

  trait TysTest extends Test with TysIfsTest {
    def taxYear: String = "2023-24"

    val uri: String =
      s"$baseUrl/income-tax/business/property/23-24/$nino/$businessId/periodic/$submissionId"

  }

  "connector" when {

    "response has uk fhl details" must {
      "return a uk ifs result" in new NonTysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse                                 = responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "return a uk tys result" in new TysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new NonTysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse                                 = responseWith(ukFhlProperty = None, ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "the tys endpoint should return a uk result" in new TysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse                                 = responseWith(ukFhlProperty = None, ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new NonTysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "the tys endpoint should return a uk result" in new TysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has no details" must {
      "return a non-uk result" in new NonTysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse                                 = responseWith(None, None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }

      "the tys endpoint return a non-uk result" in new TysTest {
        val response: RetrieveUkPropertyPeriodSummaryResponse                                 = responseWith(None, None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyPeriodSummaryResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "response is an error" must {
      "return the error" in new NonTysTest {
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }

      "the tys endpoint should return the error" in new TysTest {
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(uri, outcome)

        await(connector.retrieveUkProperty(request)) shouldBe
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }
  }
}
