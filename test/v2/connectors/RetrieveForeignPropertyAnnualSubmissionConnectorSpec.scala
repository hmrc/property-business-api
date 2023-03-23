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
import org.scalamock.handlers.CallHandler
import v2.connectors.RetrieveForeignPropertyAnnualSubmissionConnector.{ForeignResult, NonForeignResult}
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.ResponseWrapper
import v2.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v2.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val businessId: String = "XAIS12345678910"

  val countryCode: String = "FRA"

  val foreignFhlEea: ForeignFhlEeaEntry           = ForeignFhlEeaEntry(None, None)
  val foreignNonFhlProperty: ForeignPropertyEntry = ForeignPropertyEntry(countryCode, None, None)

  def responseWith(foreignFhlEea: Option[ForeignFhlEeaEntry],
                   foreignNonFhlProperty: Option[Seq[ForeignPropertyEntry]]): RetrieveForeignPropertyAnnualSubmissionResponse =
    RetrieveForeignPropertyAnnualSubmissionResponse("2020-06-17T10:53:38Z", foreignFhlEea, foreignNonFhlProperty)

  trait Test {
    _: ConnectorTest =>

    val connector: RetrieveForeignPropertyAnnualSubmissionConnector = new RetrieveForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val taxYear: String

    val request: RetrieveForeignPropertyAnnualSubmissionRequest =
      RetrieveForeignPropertyAnnualSubmissionRequest(
        Nino(nino),
        businessId,
        TaxYear.fromMtd(taxYear)
      )
  }

  trait StandardTest extends TysIfsTest with Test {

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveForeignPropertyAnnualSubmissionResponse]]]#Derived =
      willGet(
        url = s"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId"
      ).returns(Future.successful(outcome))

    lazy val taxYear: String = "2023-24"
  }

  "connector" when {
    "response has a foreign fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveForeignProperty(request)) shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "response has foreign non-fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = None, foreignNonFhlProperty = Some(Seq(foreignNonFhlProperty)))
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveForeignProperty(request)) shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "response has foreign fhl and non-fhl details" must {
      "return a foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = Some(Seq(foreignNonFhlProperty)))
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveForeignProperty(request)) shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }
    "response has no details" must {
      "return a non-foreign result" in new StandardTest {
        val response: RetrieveForeignPropertyAnnualSubmissionResponse = responseWith(None, None)
        val outcome                                                   = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        await(connector.retrieveForeignProperty(request)) shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }

    "response is an error" must {
      "return the error" in new StandardTest {
        val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)
        await(connector.retrieveForeignProperty(request)) shouldBe
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }

    "request is for a pre-TYS tax year" must {
      "use the TYS URL" in new IfsTest with Test {
        lazy val taxYear: String = "2019-20"

        val response: RetrieveForeignPropertyAnnualSubmissionResponse =
          responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)
        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/business/property/annual",
          parameters = Seq("taxableEntityId" -> nino, "incomeSourceId" -> businessId, "taxYear" -> "2019-20")
        ).returns(Future.successful(outcome))

        await(connector.retrieveForeignProperty(request)) shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }
  }
}
