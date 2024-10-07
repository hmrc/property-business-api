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

package v5.retrieveUkPropertyCumulativeSummary

import api.connectors.ConnectorSpec
import api.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import api.models.outcomes.ResponseWrapper
import v5.retrieveUkPropertyCumulativeSummary.def1.model.request.Def1_RetrieveUkPropertyCumulativeSummaryRequestData
import v5.retrieveUkPropertyCumulativeSummary.def1.model.response.{Def1_RetrieveUkPropertyCumulativeSummaryResponse, UkProperty}
import v5.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData

import scala.concurrent.Future

class RetrieveUkPropertyCumulativeSummaryConnectorSpec extends ConnectorSpec {

  private val nino       = "AA123456A"
  private val businessId = "someBusinessId"

  trait Test {
    _: ConnectorTest =>

    val connector: RetrieveUkPropertyCumulativeSummaryConnector =
      new RetrieveUkPropertyCumulativeSummaryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val requestData: RetrieveUkPropertyCumulativeSummaryRequestData =
      Def1_RetrieveUkPropertyCumulativeSummaryRequestData(Nino(nino), BusinessId(businessId), taxYear = TaxYear.fromMtd("2025-26"))

    def responseWith(ukProperty: Option[UkProperty]): Def1_RetrieveUkPropertyCumulativeSummaryResponse =
      Def1_RetrieveUkPropertyCumulativeSummaryResponse(Timestamp("2020-06-17T10:53:38Z"), "2019-01-29", "2020-03-29", ukProperty)

  }

  "RetrieveUkPropertyCumulativeSummaryConnector" when {
    "the request is made and UK property data is returned" should {
      "return UkResult" in new TysIfsTest with Test {
        private val response = responseWith(Some(UkProperty(None, None)))

        willGet(url = s"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveUkPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveUkPropertyCumulativeSummaryConnector.UkResult(response)))
      }
    }

    "the request is made and non-UK property data is returned (e.g. because the businessId is for a foreign property)" should {
      "return NonUkResult" in new TysIfsTest with Test {
        private val response = responseWith(None)

        willGet(url = s"$baseUrl/income-tax/25-26/business/property/periodic/$nino/$businessId") returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        await(connector.retrieveUkPropertyCumulativeSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveUkPropertyCumulativeSummaryConnector.NonUkResult))
      }
    }
  }

}
