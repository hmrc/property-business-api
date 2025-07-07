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

package v5.retrieveForeignPropertyCumulativeSummary

import play.api.Configuration
import shared.connectors.ConnectorSpec
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v5.retrieveForeignPropertyCumulativeSummary.def1.model.request.Def1_RetrieveForeignPropertyCumulativeSummaryRequestData
import v5.retrieveForeignPropertyCumulativeSummary.def1.model.response.{Def1_RetrieveForeignPropertyCumulativeSummaryResponse, ForeignPropertyEntry}
import v5.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData

import scala.concurrent.Future

class RetrieveForeignPropertyCumulativeSummaryConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"
  private val businessId = "someBusinessId"

  trait Test {
    _: ConnectorTest =>

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
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> false)

        private val response = responseWith(Some(Seq(ForeignPropertyEntry("AFG", None, None))))

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveForeignPropertyCumulativeSummaryConnector.ForeignResult(response)))
      }
    }

    "the request is made and non-FOREIGN property data is returned (e.g. because the businessId is for a foreign property)" should {
      "return NonForeignResult" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> false)
        private val response = responseWith(None)

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveForeignPropertyCumulativeSummaryConnector.NonForeignResult))
      }
    }

    "isPassIntentHeader feature switch is on" must {
      "pass FOREIGN_PROPERTY intent" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> true)
        private val response = responseWith(None)

        willGet(url = url"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveForeignPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveForeignPropertyCumulativeSummaryConnector.NonForeignResult))
      }
    }
  }

}
