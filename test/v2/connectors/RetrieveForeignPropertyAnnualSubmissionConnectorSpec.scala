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
import api.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult}
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequestData
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val countryCode = "FRA"

  private val foreignFhlEea         = ForeignFhlEeaEntry(None, None)
  private val foreignNonFhlProperty = ForeignPropertyEntry(countryCode, None, None)

  def responseWith(foreignFhlEea: Option[ForeignFhlEeaEntry],
                   foreignNonFhlProperty: Option[Seq[ForeignPropertyEntry]]): RetrieveForeignPropertyAnnualSubmissionResponse =
    RetrieveForeignPropertyAnnualSubmissionResponse(Timestamp("2020-06-17T10:53:38Z"), foreignFhlEea, foreignNonFhlProperty)

  "connector" when {
    "response has a foreign fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "response has foreign non-fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = None, foreignNonFhlProperty = Some(List(foreignNonFhlProperty)))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "response has foreign fhl and non-fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = Some(List(foreignNonFhlProperty)))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }
    "response has no details" must {
      "return a non-foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse = responseWith(None, None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }

    "response is an error" must {
      "return the error" in new StandardTest {
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }

    "request is for a pre-TYS tax year" must {
      "use the TYS URL" in new IfsTest with Test {
        lazy val taxYear: String = "2019-20"

        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/business/property/annual",
          parameters = List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> "2019-20")
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionConnector.Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveForeignPropertyAnnualSubmissionConnector = new RetrieveForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val taxYear: String

    protected val request: RetrieveForeignPropertyAnnualSubmissionRequestData =
      RetrieveForeignPropertyAnnualSubmissionRequestData(nino, businessId, TaxYear.fromMtd(taxYear))

  }

  trait StandardTest extends TysIfsTest with Test {

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionResponse]]]#Derived =
      willGet(
        url = s"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId"
      ).returns(Future.successful(outcome))

    lazy val taxYear: String = "2023-24"
  }

}
