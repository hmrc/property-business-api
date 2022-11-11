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

import v2.models.domain.{Nino, TaxYear}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._

import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val foreignFhlEea: AmendForeignFhlEea = AmendForeignFhlEea(
    income = Some(ForeignFhlEeaIncome(
      rentAmount = Some(567.83)
    )),
    expenses = Some(AmendForeignFhlEeaExpenses(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(5000.95),
      professionalFees = Some(23.65),
      costOfServices = Some(4777.77),
      travelCosts = Some(440.88),
      other = Some(569.75),
      consolidatedExpenses = None
    ))
  )

  private val foreignNonFhlPropertyEntry: AmendForeignNonFhlPropertyEntry = AmendForeignNonFhlPropertyEntry(
    countryCode = "FRA",
    income = Some(ForeignNonFhlPropertyIncome(
      rentIncome = Some(ForeignNonFhlPropertyRentIncome(
        rentAmount = Some(34456.30)
      )),
      foreignTaxCreditRelief = true,
      premiumsOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxPaidOrDeducted = Some(6543.01),
      specialWithholdingTaxOrUkTaxPaid = Some(643245.00)
    )),
    expenses = Some(AmendForeignNonFhlPropertyExpenses(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = None
    ))
  )

  private val requestBody: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignNonFhlProperty = Some(Seq(foreignNonFhlPropertyEntry))
  )

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: AmendForeignPropertyPeriodSummaryConnector = new AmendForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: AmendForeignPropertyPeriodSummaryRequest = AmendForeignPropertyPeriodSummaryRequest(
      nino = Nino(nino),
      businessId = businessId,
      taxYear = taxYear,
      submissionId = submissionId,
      body = requestBody
    )
  }

  "AmendForeignPropertyPeriodSummaryConnector" must {
    "send a request and return 204 no content" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

      val outcome = Right(ResponseWrapper(correlationId, ()))

      willPut(
          url = s"$baseUrl/income-tax/business/property/periodic?" +
            s"taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId",
          body = requestBody
        ).returns(Future.successful(outcome))

      val result = await(connector.amendForeignPropertyPeriodSummary(request))

      result shouldBe outcome
    }
  }

  "AmendForeignPropertyPeriodSummaryConnector called for a Tax Year Specific tax year" must {
    "send a request and return 204 no content" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

      val outcome = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url = s"$baseUrl/income-tax/business/property/periodic/${taxYear.asTysDownstream}?" +
          s"taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId",
        body = requestBody
      ).returns(Future.successful(outcome))

      val result = await(connector.amendForeignPropertyPeriodSummary(request))

      result shouldBe outcome
    }
  }
}
