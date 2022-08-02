/*
 * Copyright 2022 HM Revenue & Customs
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

import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockHttpClient
import v2.models.domain.{Nino, TaxYear}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.{CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody, HistoricFhlAnnualAdjustments, HistoricFhlAnnualAllowances}

import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA123456A"
  val mtdTaxYear: String = "2019-20"
  val downstreamTaxYear: String = "2020"

  private val annualAdjustments = HistoricFhlAnnualAdjustments(
    Some(BigDecimal("105.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("120.11")),
    true,
    Some(BigDecimal("101.11")),
    false,
    Some(UkPropertyAdjustmentsRentARoom(true))
  )

  private val annualAllowances = HistoricFhlAnnualAllowances(
    Some(BigDecimal("100.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("425.11")),
    Some(BigDecimal("550.11"))
  )

  val body: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(
    Some(annualAdjustments),
    Some(annualAllowances)
  )

  val request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(mtdTaxYear),
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    "put a body and return a 204" in new Test {
      private val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredDesHeadersPut: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$downstreamTaxYear",
          config = dummyDesHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredDesHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome

    }
  }
}