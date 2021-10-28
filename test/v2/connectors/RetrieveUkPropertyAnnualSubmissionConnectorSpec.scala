/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import v2.connectors.RetrieveUkPropertyAnnualSubmissionConnector._
import v2.mocks.MockHttpClient
import v2.models.domain.Nino
import v2.models.errors.{ IfsErrorCode, IfsErrors }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionResponse
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty.UkFhlProperty
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlProperty

import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String    = "2019-20"

  val request: RetrieveUkPropertyAnnualSubmissionRequest = RetrieveUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear
  )

  val ukFhlProperty: UkFhlProperty       = UkFhlProperty(None, None)
  val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(None, None)

  def responseWith(ukFhlProperty: Option[UkFhlProperty], ukNonFhlProperty: Option[UkNonFhlProperty]): RetrieveUkPropertyAnnualSubmissionResponse =
    RetrieveUkPropertyAnnualSubmissionResponse("2020-01-01", ukFhlProperty, ukNonFhlProperty)

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveUkPropertyAnnualSubmissionConnector = new RetrieveUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)

    def stubHttpResponse(outcome: IfsOutcome[RetrieveUkPropertyAnnualSubmissionResponse])
      : CallHandler[Future[IfsOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]]#Derived = {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/business/property/annual",
          config = dummyIfsHeaderCarrierConfig,
          queryParams = Seq("taxableEntityId" -> nino, "incomeSourceId" -> businessId, "taxYear" -> taxYear),
          requiredHeaders = requiredIfsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))
    }
  }

  "connector" when {
    "response has uk fhl details" must {
      "return a uk result" in new Test {
        val response: RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new Test {
        val response: RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukFhlProperty = None, ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new Test {
        val response: RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has no details" must {
      "return a non-uk result" in new Test {
        val response: RetrieveUkPropertyAnnualSubmissionResponse = responseWith(None, None)
        val outcome                                              = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "response is an error" must {
      "return the error" in new Test {
        val outcome = Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        await(connector.retrieveUkProperty(request)) shouldBe
          Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode("SOME_ERROR"))))
      }
    }
  }
}