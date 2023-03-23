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

import api.connectors.ConnectorSpec
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.{CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody, HistoricFhlAnnualAdjustments, HistoricFhlAnnualAllowances}
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  private val annualAdjustments = HistoricFhlAnnualAdjustments(
    Some(BigDecimal("105.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("120.11")),
    periodOfGraceAdjustment = true,
    Some(BigDecimal("101.11")),
    nonResidentLandlord = false,
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

  trait Test {
    _: ConnectorTest =>

    val connector = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      private val outcome = Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))

      willPut(
        url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/$downstreamTaxYear",
        body = body
      ).returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome

    }
  }
}
