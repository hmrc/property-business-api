/*
 * Copyright 2026 HM Revenue & Customs
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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveUkPropertyAnnualSubmission.def1.model.request.Def1_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.RetrieveUkFhlProperty as RetrieveUkFhlPropertyDef1
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.RetrieveUkProperty as RetrieveUkPropertyDef1
import v6.retrieveUkPropertyAnnualSubmission.def2.model.request.Def2_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.def2.model.response.{
  Def2_RetrieveUkPropertyAnnualSubmissionResponse,
  RetrieveUkProperty as RetrieveUkPropertyDef2
}
import v6.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.model.{NonUkResult, Result, UkResult}

import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: Nino             = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val timestamp: Timestamp   = Timestamp("2022-06-17T10:53:38Z")

  private val ukFhlPropertyDef1: RetrieveUkFhlPropertyDef1 = RetrieveUkFhlPropertyDef1(None, None)
  private val ukPropertyDef1: RetrieveUkPropertyDef1       = RetrieveUkPropertyDef1(None, None)
  private val ukPropertyDef2: RetrieveUkPropertyDef2       = RetrieveUkPropertyDef2(None, None)

  def def1Response(ukFhlProperty: Option[RetrieveUkFhlPropertyDef1],
                   ukProperty: Option[RetrieveUkPropertyDef1]): Def1_RetrieveUkPropertyAnnualSubmissionResponse =
    Def1_RetrieveUkPropertyAnnualSubmissionResponse(timestamp, ukFhlProperty, ukProperty)

  def def2Response(ukProperty: Option[RetrieveUkPropertyDef2]): Def2_RetrieveUkPropertyAnnualSubmissionResponse =
    Def2_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp("2025-06-17T10:53:38Z"), ukProperty)

  "RetrieveUkPropertyAnnualSubmissionConnector" should {
    "return a uk result" when {
      "the request for pre-TYS tax year returns a response with uk fhl and property details" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          def1Response(Some(ukFhlPropertyDef1), Some(ukPropertyDef1))

        val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/business/property/annual",
          parameters = List(
            "taxableEntityId" -> nino.nino,
            "incomeSourceId"  -> businessId.businessId,
            "taxYear"         -> taxYear.asMtd
          )
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "the request for tax year 2025-26 returns a response with uk property details" in new HipTest with Test {
        val response: Def2_RetrieveUkPropertyAnnualSubmissionResponse = def2Response(Some(ukPropertyDef2))

        val outcome: Right[Nothing, ResponseWrapper[Def2_RetrieveUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

        willGet(url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "the request for tax year returns a response" which {

        "has only uk fhl details for tax year 2024-25" in new HipTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2024-25")

          val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = def1Response(Some(ukFhlPropertyDef1), None)

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveUkPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
          result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
        }

        "has only uk fhl details for tax year 2023-24" in new HipTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = def1Response(Some(ukFhlPropertyDef1), None)

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveUkPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
          result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
        }

        "has only uk property details for tax year 2023-24" in new HipTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = def1Response(None, Some(ukPropertyDef1))

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveUkPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
          result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
        }

        "has both uk fhl and property details for tax year 2023-24" in new HipTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
            def1Response(Some(ukFhlPropertyDef1), Some(ukPropertyDef1))

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveUkPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
          result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
        }
      }
    }

    "return a non-uk result" when {
      "the request for tax year 2025-26 returns a response with no uk details" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

        val response: Def2_RetrieveUkPropertyAnnualSubmissionResponse = def2Response(None)

        val outcome: Right[Nothing, ResponseWrapper[Def2_RetrieveUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(
          url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "return an error when downstream call fails" in new HipTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

      val response: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))

      val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
        Left(ResponseWrapper(correlationId, response))

      willGet(
        url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
      result shouldBe outcome
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected def taxYear: TaxYear

    protected val connector: RetrieveUkPropertyAnnualSubmissionConnector = new RetrieveUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: RetrieveUkPropertyAnnualSubmissionRequestData = taxYear.year match {
      case year if year >= 2026 => Def2_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)
      case _                    => Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)
    }

  }

}
