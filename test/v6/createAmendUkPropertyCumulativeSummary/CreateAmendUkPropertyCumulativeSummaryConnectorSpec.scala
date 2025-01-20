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

package v6.createAmendUkPropertyCumulativeSummary

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v6.createAmendUkPropertyCumulativeSummary.def1.model.request._
import v6.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData

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
    "post a body and return 204" in new TysIfsTest with Test {
      lazy val taxYear: TaxYear = TaxYear.fromMtd("2025-26")

      willPut(
        url = s"$baseUrl/income-tax/${taxYear.asTysDownstream}/business/property/periodic/${nino.value}/${businessId.businessId}",
        body = requestBody
      ) returns Future.successful(Right(ResponseWrapper(correlationId, ())))

      val result: DownstreamOutcome[Unit] =
        await(connector.createAmendUkPropertyCumulativeSummary(requestData))
      result shouldBe Right(ResponseWrapper(correlationId, ()))
    }

  }

  trait Test { _: ConnectorTest =>

    protected val taxYear: TaxYear

    protected val requestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
      Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(Some("2020-01-01"), Some("2020-01-31"), ukProperty = ukProperty)

    protected val requestData: CreateAmendUkPropertyCumulativeSummaryRequestData =
      Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(nino, taxYear, businessId, requestBody)

    protected val connector: CreateAmendUkPropertyCumulativeSummaryConnector = new CreateAmendUkPropertyCumulativeSummaryConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

}
