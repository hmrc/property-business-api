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

package v6.retrieveUkPropertyAnnualSubmission

import org.scalamock.handlers.CallHandler
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionConnector._
import v6.retrieveUkPropertyAnnualSubmission.def1.model.request.Def1_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.RetrieveUkFhlProperty
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.RetrieveUkProperty
import v6.retrieveUkPropertyAnnualSubmission.model.response._

import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val ukFhlProperty = RetrieveUkFhlProperty(None, None)
  private val ukProperty    = RetrieveUkProperty(None, None)

  "connector" when {
    "response has uk fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukFhlProperty = None, ukProperty = Some(ukProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukProperty = Some(ukProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has no details" must {
      "return a non-uk result" in new StandardTest {
        val response: RetrieveUkPropertyAnnualSubmissionResponse                                 = responseWith(None, None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "response is an error" must {
      "return the error" in new StandardTest {
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }

    "request is for a pre-TYS tax year" must {
      "use the pre-TYS URL" in new IfsTest with Test {
        lazy val taxYear: String = "2019-20"

        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(Some(ukFhlProperty), ukProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/business/property/annual",
          parameters = List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveUkPropertyAnnualSubmissionConnector = new RetrieveUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val taxYear: String

    protected val request: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, TaxYear.fromMtd(taxYear))

    def responseWith(ukFhlProperty: Option[RetrieveUkFhlProperty],
                     ukProperty: Option[RetrieveUkProperty]): Def1_RetrieveUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp("2022-06-17T10:53:38Z"), ukFhlProperty, ukProperty)

  }

  trait StandardTest extends IfsTest with Test {

    protected lazy val taxYear = "2023-24"

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]]#Derived = {
      willGet(
        url = url"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId"
      ).returns(Future.successful(outcome))
    }

  }

}
