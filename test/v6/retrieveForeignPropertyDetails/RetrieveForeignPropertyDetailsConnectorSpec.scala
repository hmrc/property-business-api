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

package v6.retrieveForeignPropertyDetails

import common.models.domain.PropertyId
import play.api.Configuration
import shared.connectors.ConnectorSpec
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveForeignPropertyDetails.def1.model.request.Def1_RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.def1.model.response.{Def1_RetrieveForeignPropertyDetailsResponse, ForeignPropertyDetailsEntry}
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.model.ForeignResult

import scala.concurrent.Future

class RetrieveForeignPropertyDetailsConnectorSpec extends ConnectorSpec {

  private val nino          = "AA123456A"
  private val businessId    = "someBusinessId"
  private val taxYear       = "2026-27"
  private val propertyId    = "8e8b8450-dc1b-4360-8109-7067337b42cb"
  private val downstreamUrl = url"$baseUrl/itsd/income-sources/$nino/foreign-property-details/$businessId"

  private val foreignPropertyDetailsEntry = ForeignPropertyDetailsEntry(
    Timestamp("2026-07-07T10:59:47.544Z"),
    propertyId,
    "Bob & Bobby Co",
    "FRA",
    None,
    None
  )

  trait Test {
    self: ConnectorTest =>

    val connector: RetrieveForeignPropertyDetailsConnector =
      new RetrieveForeignPropertyDetailsConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    def responseWith(details: Seq[ForeignPropertyDetailsEntry]): Def1_RetrieveForeignPropertyDetailsResponse =
      Def1_RetrieveForeignPropertyDetailsResponse(details)

  }

  "RetrieveForeignPropertyDetailsConnector" when {
    "the request is made and FOREIGN property data is returned" should {
      "return ForeignResult for Some propertyId" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig
          .anyNumberOfTimes()
          .returns(
            Configuration("passIntentHeader.enabled" -> false)
          )

        private val response = responseWith(Seq(foreignPropertyDetailsEntry))

        private val maximumRequestParams = List(
          "taxYear"    -> taxYear,
          "propertyId" -> propertyId
        )

        val maximumRequestData: RetrieveForeignPropertyDetailsRequestData =
          Def1_RetrieveForeignPropertyDetailsRequestData(
            Nino(nino),
            BusinessId(businessId),
            TaxYear.fromMtd(taxYear),
            Some(PropertyId(propertyId))
          )

        willGet(url = downstreamUrl, parameters = maximumRequestParams).returns(
          Future.successful(Right(ResponseWrapper(correlationId, response)))
        )

        await(connector.retrieveForeignPropertyDetails(maximumRequestData)).shouldBe(
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
        )
      }

      "return ForeignResult for None propertyId" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig
          .anyNumberOfTimes()
          .returns(
            Configuration("passIntentHeader.enabled" -> false)
          )

        private val response = responseWith(Seq(foreignPropertyDetailsEntry))

        private val minimumParams = List(
          "taxYear" -> taxYear
        )

        val minimumRequestData: RetrieveForeignPropertyDetailsRequestData =
          Def1_RetrieveForeignPropertyDetailsRequestData(
            Nino(nino),
            BusinessId(businessId),
            TaxYear.fromMtd(taxYear),
            None
          )

        willGet(url = downstreamUrl, parameters = minimumParams).returns(
          Future.successful(Right(ResponseWrapper(correlationId, response)))
        )

        await(connector.retrieveForeignPropertyDetails(minimumRequestData)).shouldBe(
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
        )
      }
    }
  }

}
