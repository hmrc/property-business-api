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

package v3.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear, Timestamp}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import v3.connectors.RetrieveForeignPropertyPeriodSummaryConnector.{ForeignResult, NonForeignResult}
import v3.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequestData
import v3.models.response.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryResponse
import v3.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v3.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._

import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  private val nino         = Nino("AA123456A")
  private val businessId   = BusinessId("XAIS12345678910")
  private val submissionId = SubmissionId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
  private val countryCode  = "FRA"

  private val foreignFhlEea         = ForeignFhlEea(None, None)
  private val foreignNonFhlProperty = ForeignNonFhlProperty(countryCode, None, None)

  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  def responseWith(foreignFhlEea: Option[ForeignFhlEea],
                   foreignNonFhlProperty: Option[Seq[ForeignNonFhlProperty]]): RetrieveForeignPropertyPeriodSummaryResponse =
    RetrieveForeignPropertyPeriodSummaryResponse(Timestamp("2020-06-17T10:53:38Z"), "2019-01-29", "2020-03-29", foreignFhlEea, foreignNonFhlProperty)

  "connector" when {
    "response has foreign FHL details" must {

      val downstreamResponse: RetrieveForeignPropertyPeriodSummaryResponse =
        responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = None)
      val outcome = Right(ResponseWrapper(correlationId, downstreamResponse))

      "return a foreign result" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }

      "return a foreign result given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }
    }

    "response has foreign non-FHL details" must {

      val downstreamResponse: RetrieveForeignPropertyPeriodSummaryResponse =
        responseWith(foreignFhlEea = None, foreignNonFhlProperty = Some(List(foreignNonFhlProperty)))
      val outcome = Right(ResponseWrapper(correlationId, downstreamResponse))

      "return a foreign result" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }

      "return a foreign result given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }
    }

    "response has foreign FHL and non-FHL details" must {

      val downstreamResponse: RetrieveForeignPropertyPeriodSummaryResponse =
        responseWith(foreignFhlEea = Some(foreignFhlEea), foreignNonFhlProperty = Some(List(foreignNonFhlProperty)))
      val outcome = Right(ResponseWrapper(correlationId, downstreamResponse))

      "return a foreign result" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }

      "return a foreign result given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(downstreamResponse)))
      }
    }

    "response has no details" must {

      val downstreamResponse: RetrieveForeignPropertyPeriodSummaryResponse =
        responseWith(None, None)
      val outcome = Right(ResponseWrapper(correlationId, downstreamResponse))

      "return a non-foreign result" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }

      "return a non-foreign result given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }

    "response is an error" must {

      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new IfsTest with Test {
        lazy val taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryConnector.Result] =
          await(connector.retrieveForeignProperty(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val connector: RetrieveForeignPropertyPeriodSummaryConnector = new RetrieveForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: RetrieveForeignPropertyPeriodSummaryRequestData =
      RetrieveForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, submissionId)

    protected def stubHttpResponse(outcome: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryResponse]): Unit =
      willGet(
        url = s"$baseUrl/income-tax/business/property/periodic",
        parameters = List(
          "taxableEntityId" -> nino.nino,
          "incomeSourceId"  -> businessId.businessId,
          "taxYear"         -> taxYear.asMtd,
          "submissionId"    -> submissionId.submissionId)
      ).returns(Future.successful(outcome))

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[RetrieveForeignPropertyPeriodSummaryResponse]): Unit =
      willGet(
        url = s"$baseUrl/income-tax/business/property/${taxYear.asTysDownstream}/$nino/$businessId/periodic/$submissionId"
      ).returns(Future.successful(outcome))

  }

}
