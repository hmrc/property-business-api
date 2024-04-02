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

package v4.controllers.retrieveUkPropertyAnnualSubmission

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v4.controllers.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionConnector._
import v4.controllers.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukFhlProperty.Def1_Retrieve_UkFhlProperty
import v4.controllers.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty.Def1_Retrieve_UkNonFhlProperty
import v4.controllers.retrieveUkPropertyAnnualSubmission.model.request._
import v4.controllers.retrieveUkPropertyAnnualSubmission.model.response._

import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val ukFhlProperty    = Def1_Retrieve_UkFhlProperty(None, None)
  private val ukNonFhlProperty = Def1_Retrieve_UkNonFhlProperty(None, None)

  "connector" when {
    "response has uk fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukFhlProperty = None, ukNonFhlProperty = Some(ukNonFhlProperty))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new StandardTest {
        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty))
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
          responseWith(Some(ukFhlProperty), ukNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/business/property/annual",
          parameters = Seq("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear)
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
      appConfig = mockAppConfig
    )

    protected val taxYear: String

    protected val request: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, TaxYear.fromMtd(taxYear))

    def responseWith(ukFhlProperty: Option[Def1_Retrieve_UkFhlProperty],
                     ukNonFhlProperty: Option[Def1_Retrieve_UkNonFhlProperty]): Def1_RetrieveUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp("2022-06-17T10:53:38Z"), ukFhlProperty, ukNonFhlProperty)

  }

  trait StandardTest extends TysIfsTest with Test {

    protected lazy val taxYear = "2023-24"

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId"
      ).returns(Future.successful(outcome))
    }

  }

}
