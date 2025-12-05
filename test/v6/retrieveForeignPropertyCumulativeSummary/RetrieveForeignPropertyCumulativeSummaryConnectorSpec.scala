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

package v6.retrieveForeignPropertyCumulativeSummary

import common.models.domain.PropertyId
import play.api.Configuration
import shared.connectors.ConnectorSpec
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveForeignPropertyCumulativeSummary.def1.model.request.Def1_RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.request.Def2_RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.def1.model.response.{Def1_RetrieveForeignPropertyCumulativeSummaryResponse, ForeignPropertyEntry}
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.response.Def2_RetrieveForeignPropertyCumulativeSummaryResponse
import v6.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.model.{ForeignResult, NonForeignResult, Result}

import scala.concurrent.Future

class RetrieveForeignPropertyCumulativeSummaryConnectorSpec extends ConnectorSpec {

  private val nino       = "AA123456A"
  private val businessId = "someBusinessId"
  private val propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  trait Test {
    self: ConnectorTest =>

    val connector: RetrieveForeignPropertyCumulativeSummaryConnector =
      new RetrieveForeignPropertyCumulativeSummaryConnector(http = mockHttpClient)

    val requestData: RetrieveForeignPropertyCumulativeSummaryRequestData =
      Def1_RetrieveForeignPropertyCumulativeSummaryRequestData(Nino(nino), BusinessId(businessId), taxYear = TaxYear.fromMtd("2025-26"))

    def responseWith(foreignProperty: Option[Seq[ForeignPropertyEntry]]): Def1_RetrieveForeignPropertyCumulativeSummaryResponse =
      Def1_RetrieveForeignPropertyCumulativeSummaryResponse(Timestamp("2020-06-17T10:53:38Z"), "2019-01-29", "2020-03-29", foreignProperty)

  }

  "RetrieveForeignPropertyCumulativeSummaryConnector" when {
    "the request is made and FOREIGN property data is returned" should {
      "return ForeignResult" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns
          Configuration(
            "ifs_hip_migration_1962.enabled" -> false,
            "passIntentHeader.enabled"       -> false
          )
        private val response = responseWith(Some(Seq(ForeignPropertyEntry("AFG", None, None))))

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))
        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "the request is made and FOREIGN property data is returned for HIP enabled for TYS 25-26" should {
      "return ForeignResult" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1962.enabled" -> true))
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> false)

        private val response = responseWith(Some(Seq(ForeignPropertyEntry("AFG", None, None))))
        willGet(url = url"$baseUrl/itsa/income-tax/v1/25-26/business/periodic/property/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "the request is made and FOREIGN property data is returned for HIP enabled for TYS 26-27" should {
      "return ForeignResult" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1962.enabled" -> true))
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> false)

        override val requestData: RetrieveForeignPropertyCumulativeSummaryRequestData =
          Def2_RetrieveForeignPropertyCumulativeSummaryRequestData(
            Nino(nino),
            BusinessId(businessId),
            taxYear = TaxYear.fromMtd("2026-27"),
            Some(PropertyId(propertyId)))
        def responseWith(foreignProperty: Seq[def2.model.response.ForeignPropertyEntry]): Def2_RetrieveForeignPropertyCumulativeSummaryResponse =
          Def2_RetrieveForeignPropertyCumulativeSummaryResponse(Timestamp("2020-06-17T10:53:38Z"), "2019-01-29", "2020-03-29", foreignProperty)
        private val response = responseWith(Seq(def2.model.response.ForeignPropertyEntry("8e8b8450-dc1b-4360-8109-7067337b42cb", None, None)))
        willGet(
          url = url"$baseUrl/itsa/income-tax/v1/26-27/business/periodic/foreign-property/$nino/$businessId",
          parameters = Seq(
            "propertyId" -> propertyId
          )) returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }

    "the request is made and non-FOREIGN property data is returned (e.g. because the businessId is for a foreign property)" should {
      "return NonForeignResult" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns
          Configuration(
            "ifs_hip_migration_1962.enabled" -> false,
            "passIntentHeader.enabled"       -> false
          )
        private val response = responseWith(None)

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }

    "isPassIntentHeader feature switch is on" must {
      "pass FOREIGN_PROPERTY intent" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns
          Configuration(
            "ifs_hip_migration_1962.enabled" -> false,
            "passIntentHeader.enabled"       -> true
          )

        private val response = responseWith(None)

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, NonForeignResult))
      }
    }
  }

}
