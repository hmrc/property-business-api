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

package v6.retrieveForeignPropertyAnnualSubmission

import common.models.domain.PropertyId
import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveForeignPropertyAnnualSubmission.model.{ForeignResult, NonForeignResult, Result}
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.Def1_RetrieveForeignPropertyAnnualSubmissionResponse
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignFhlEea.RetrieveForeignFhlEeaEntry
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty.RetrieveForeignPropertyEntry as Def1_ForeignPropertyEntry
import v6.retrieveForeignPropertyAnnualSubmission.def1.request.Def1_RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.def2.model.response.{
  Def2_RetrieveForeignPropertyAnnualSubmissionResponse,
  RetrieveForeignPropertyEntry as Def2_ForeignPropertyEntry
}
import v6.retrieveForeignPropertyAnnualSubmission.def2.request.Def2_RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.def3.fixture.Def3_RetrieveForeignPropertyAnnualSubmissionFixture.fullResponseModel
import v6.retrieveForeignPropertyAnnualSubmission.def3.model.response.Def3_RetrieveForeignPropertyAnnualSubmissionResponse
import v6.retrieveForeignPropertyAnnualSubmission.def3.request.Def3_RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino: Nino             = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val propertyId: PropertyId = PropertyId("8e8b8450-dc1b-4360-8109-7067337b42cb")
  private val timestamp: Timestamp   = Timestamp("2020-06-17T10:53:38Z")

  private val foreignFhlEeaEntry: RetrieveForeignFhlEeaEntry      = RetrieveForeignFhlEeaEntry(None, None)
  private val def1ForeignPropertyEntry: Def1_ForeignPropertyEntry = Def1_ForeignPropertyEntry("FRA", None, None)
  private val def2ForeignPropertyEntry: Def2_ForeignPropertyEntry = Def2_ForeignPropertyEntry("FRA", None, None)

  def def1Response(foreignFhlEea: Option[RetrieveForeignFhlEeaEntry], foreignProperty: Option[Seq[Def1_ForeignPropertyEntry]]) =
    Def1_RetrieveForeignPropertyAnnualSubmissionResponse(timestamp, foreignFhlEea, foreignProperty)

  def def2Response(foreignProperty: Option[Seq[Def2_ForeignPropertyEntry]]): Def2_RetrieveForeignPropertyAnnualSubmissionResponse =
    Def2_RetrieveForeignPropertyAnnualSubmissionResponse(timestamp, foreignProperty)

  val def3Response: Def3_RetrieveForeignPropertyAnnualSubmissionResponse = fullResponseModel

  "RetrieveForeignPropertyAnnualSubmissionConnector" should {
    "return a foreign result" when {
      "the request for tax year 2026-27 onwards returns a response with foreign property details" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val outcome: Right[Nothing, ResponseWrapper[Def3_RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, def3Response))

        willGet(
          url = url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/foreign-property/annual/$nino/$businessId",
          parameters = List("propertyId" -> propertyId.propertyId)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(def3Response)))
      }

      "the request for tax year 2025-26 returns a response with foreign property details" which {
        val response: Def2_RetrieveForeignPropertyAnnualSubmissionResponse = def2Response(Some(List(def2ForeignPropertyEntry)))

        val outcome: Right[Nothing, ResponseWrapper[Def2_RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        "is from HIP downstream when the feature switch is enabled" in new HipTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

          MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1805.enabled" -> true))

          willGet(
            url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

          result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
        }

        "is from IFS downstream when the feature switch is disabled" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

          MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1805.enabled" -> false))

          willGet(
            url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

          result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
        }
      }

      "the request for tax year 2023-24 returns a response" which {
        "has only foreign fhl details" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveForeignPropertyAnnualSubmissionResponse = def1Response(Some(foreignFhlEeaEntry), None)

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

          result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
        }

        "has only foreign property details" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveForeignPropertyAnnualSubmissionResponse = def1Response(None, Some(List(def1ForeignPropertyEntry)))

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

          result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
        }

        "has both foreign fhl and property details" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val response: Def1_RetrieveForeignPropertyAnnualSubmissionResponse =
            def1Response(Some(foreignFhlEeaEntry), Some(List(def1ForeignPropertyEntry)))

          val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]] =
            Right(ResponseWrapper(correlationId, response))

          willGet(
            url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"
          ).returns(Future.successful(outcome))

          val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

          result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
        }
      }

      "the request for pre-TYS tax year returns a response with foreign fhl and property details" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val response: Def1_RetrieveForeignPropertyAnnualSubmissionResponse =
          def1Response(Some(foreignFhlEeaEntry), Some(List(def1ForeignPropertyEntry)))

        val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/business/property/annual",
          parameters = List(
            "taxableEntityId" -> nino.nino,
            "incomeSourceId"  -> businessId.businessId,
            "taxYear"         -> taxYear.asMtd
          )
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "return a non-foreign result" when {
      "the request for tax year 2025-26 returns a response with no foreign details" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2025-26")

        val response: Def2_RetrieveForeignPropertyAnnualSubmissionResponse = def2Response(None)

        val outcome: Right[Nothing, ResponseWrapper[Def2_RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1805.enabled" -> true))

        willGet(
          url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/property/annual/$nino/$businessId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

        result shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }

      "the request for pre-2025-26 tax year returns a response with no foreign details" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val response: Def1_RetrieveForeignPropertyAnnualSubmissionResponse = def1Response(None, None)

        val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(
          url"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

        result shouldBe Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }

    "return an error when downstream call fails" in new HipTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

      val response: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))

      val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
        Left(ResponseWrapper(correlationId, response))

      willGet(
        url = url"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/business/foreign-property/annual/$nino/$businessId",
        parameters = List("propertyId" -> propertyId.propertyId)
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[Result] = await(connector.retrieveForeignProperty(request))

      result shouldBe outcome
    }

  }

  trait Test {
    self: ConnectorTest =>

    protected val connector: RetrieveForeignPropertyAnnualSubmissionConnector = new RetrieveForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected def taxYear: TaxYear

    protected val request: RetrieveForeignPropertyAnnualSubmissionRequestData = taxYear.year match {
      case year if year >= 2027 => Def3_RetrieveForeignPropertyAnnualSubmissionRequestData(nino, businessId, taxYear, Some(propertyId))
      case 2026                 => Def2_RetrieveForeignPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)
      case _                    => Def1_RetrieveForeignPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)
    }

  }

}
