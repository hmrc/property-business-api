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
import v6.retrieveForeignPropertyDetails.model.{ForeignResult, NonForeignResult, Result}

import scala.concurrent.Future

class RetrieveForeignPropertyDetailsConnectorSpec extends ConnectorSpec {

  private val nino       = "AA123456A"
  private val businessId = "someBusinessId"
  private val propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  trait Test {
    self: ConnectorTest =>

    val connector: RetrieveForeignPropertyDetailsConnector =
      new RetrieveForeignPropertyDetailsConnector(http = mockHttpClient)

    val requestData: RetrieveForeignPropertyDetailsRequestData =
      Def1_RetrieveForeignPropertyDetailsRequestData(
        Nino(nino),
        BusinessId(businessId),
        TaxYear.fromMtd("2025-26"),
        PropertyId(propertyId)
      )

    def responseWith(foreignPropertyDetails: Seq[ForeignPropertyDetailsEntry]): Def1_RetrieveForeignPropertyDetailsResponse =
      Def1_RetrieveForeignPropertyDetailsResponse(foreignPropertyDetails)

  }

  "RetrieveForeignPropertyDetailsConnector" when {
    "the request is made and FOREIGN property data is returned" should {
      "return ForeignResult" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> false)

        private val response = responseWith(
          Seq(
            ForeignPropertyDetailsEntry(
              "2026-07-07T10:59:47.544Z",
              propertyId,
              "Bob & Bobby Co",
              "FRA"
            )))

        willGet(url = url"$baseUrl/itsd/income-sources/$nino/foreign-property-details/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyDetails(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, ForeignResult(response)))
      }
    }
  }

}
