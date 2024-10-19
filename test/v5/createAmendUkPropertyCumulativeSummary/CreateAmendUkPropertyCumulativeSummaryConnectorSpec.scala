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

package v5.createAmendUkPropertyCumulativeSummary

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request.{
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody,
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestData,
  _
}
import v5.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData
import v5.createAmendUkPropertyCumulativeSummary.model.response.CreateAmendUkPropertyCumulativeSummaryResponse

import scala.concurrent.Future

class CreateAmendUkPropertyCumulativeSummaryConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  val ukProperty: UkProperty = UkProperty(
    income = Some(
      Income(
        premiumsOfLeaseGrant = Some(42.12),
        reversePremiums = Some(84.31),
        periodAmount = Some(9884.93),
        taxDeducted = Some(842.99),
        otherIncome = Some(31.44),
        rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
      )
    ),
    expenses = Some(
      Expenses(
        premisesRunningCosts = None,
        repairsAndMaintenance = None,
        financialCosts = None,
        professionalFees = None,
        costOfServices = None,
        other = None,
        residentialFinancialCost = Some(9000.10),
        travelCosts = None,
        residentialFinancialCostsCarriedForward = Some(300.13),
        rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
        consolidatedExpenses = Some(-988.18)
      )
    )
  )

  "connector" must {
    "post a body and return 200 with submissionId" in new TysIfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2025-26")

      willPut(
        url = s"$baseUrl/income-tax/${taxYear.asTysDownstream}/business/property/periodic/${nino.value}/${businessId.businessId}",
        body = requestBody
      ) returns Future.successful(outcome)

      val result: DownstreamOutcome[CreateAmendUkPropertyCumulativeSummaryResponse] =
        await(connector.createAmendUkPropertyCumulativeSummary(requestData))
      result shouldBe outcome
    }

  }

  trait Test { _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val requestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
      Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody("2020-01-01", "2020-01-31", ukProperty = ukProperty)

    protected val requestData: CreateAmendUkPropertyCumulativeSummaryRequestData =
      Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(nino, taxYear, businessId, requestBody)

    protected val response: CreateAmendUkPropertyCumulativeSummaryResponse = CreateAmendUkPropertyCumulativeSummaryResponse(
      "4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

    protected val outcome: Right[Nothing, ResponseWrapper[CreateAmendUkPropertyCumulativeSummaryResponse]] = Right(
      ResponseWrapper(correlationId, response))

    protected val connector: CreateAmendUkPropertyCumulativeSummaryConnector = new CreateAmendUkPropertyCumulativeSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
