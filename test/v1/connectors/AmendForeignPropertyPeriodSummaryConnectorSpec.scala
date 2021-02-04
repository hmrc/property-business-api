/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignPropertyPeriodSummary.{AmendForeignPropertyPeriodSummaryRequest, AmendForeignPropertyPeriodSummaryRequestBody}
import v1.models.request.common.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenditure, ForeignFhlEeaIncome}
import v1.models.request.common.foreignPropertyEntry.{ForeignPropertyEntry, ForeignPropertyExpenditure, ForeignPropertyIncome, ForeignPropertyRentIncome}

import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val foreignFhlEea: ForeignFhlEea = ForeignFhlEea(
    income = ForeignFhlEeaIncome(rentAmount = 567.83, taxDeducted = Some(4321.92)),
    expenditure = Some(ForeignFhlEeaExpenditure(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(4566.95),
      professionalFees = Some(23.65),
      costsOfServices = Some(4567.77),
      travelCosts = Some(456.77),
      other = Some(567.67),
      consolidatedExpenses = Some(456.98)
    ))
  )

  private val foreignProperty: ForeignPropertyEntry = ForeignPropertyEntry(
    countryCode = "zzz",
    income = ForeignPropertyIncome(
      rentIncome = ForeignPropertyRentIncome(rentAmount = 34456.30, taxDeducted = 6334.34),
      foreignTaxCreditRelief = true,
      premiumOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxTakenOff = Some(6543.01),
      specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
    ),
    expenditure = Some(ForeignPropertyExpenditure(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costsOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = Some(352.66)
    ))
  )

  val body: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignProperty = Some(Seq(foreignProperty))
  )

  val request = AmendForeignPropertyPeriodSummaryRequest(nino, businessId, submissionId, body)

  val response = ()

  class Test extends MockHttpClient with MockAppConfig {
    val connector: AmendForeignPropertyPeriodSummaryConnector = new AmendForeignPropertyPeriodSummaryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "connector" must {
    "put a body and return 204 no body" in new Test {

      val outcome = Right(ResponseWrapper(correlationId, response))
      MockedHttpClient
        .put(
          url = s"$baseUrl/business/property/${nino}/${businessId}/period/${submissionId}",
          body = body,
          requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.amendForeignProperty(request)) shouldBe outcome

    }
  }
}