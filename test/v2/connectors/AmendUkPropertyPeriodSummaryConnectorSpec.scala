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

import mocks.{MockAppConfig, MockHttpClient}
import uk.gov.hmrc.http.HeaderCarrier
import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyPeriodSummary.{AmendUkPropertyPeriodSummaryRequest, AmendUkPropertyPeriodSummaryRequestBody}
import v2.models.request.common.ukFhlProperty._
import v2.models.request.common.ukNonFhlProperty._
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val taxYear: String = "2022-23"
  val businessId: String = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestBody: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(
    ukFhlProperty = Some(UkFhlProperty(
      income = Some(UkFhlPropertyIncome(
        periodAmount = Some(5000.99),
        taxDeducted = Some(3123.21),
        rentARoom = Some(UkPropertyIncomeRentARoom(
          rentsReceived = Some(532.12)
        ))
      )),
      expenses = Some(UkFhlPropertyExpenses(
        premisesRunningCosts = Some(3120.23),
        repairsAndMaintenance = Some(928.42),
        financialCosts = Some(842.99),
        professionalFees = Some(8831.12),
        costOfServices = Some(484.12),
        other = Some(99282.52),
        consolidatedExpenses = None,
        travelCosts = Some(974.47),
        rentARoom = Some(UkPropertyExpensesRentARoom(
          amountClaimed = Some(8842.43)
        ))
      ))
    )),
    ukNonFhlProperty = Some(UkNonFhlProperty(
      income = Some(UkNonFhlPropertyIncome(
        premiumsOfLeaseGrant = Some(41.12),
        reversePremiums = Some(84.31),
        periodAmount = Some(9884.93),
        taxDeducted = Some(855.99),
        otherIncome = Some(31.44),
        rentARoom = Some(UkPropertyIncomeRentARoom(
          rentsReceived = Some(947.66)
        ))
      )),
      expenses = Some(UkNonFhlPropertyExpenses(
        premisesRunningCosts = Some(3200.25),
        repairsAndMaintenance = Some(950.45),
        financialCosts = Some(830.99),
        professionalFees = Some(7500.70),
        costOfServices = Some(400.30),
        other = Some(95000.55),
        residentialFinancialCost = Some(999.99),
        travelCosts = Some(960.75),
        residentialFinancialCostsCarriedForward = Some(8500.12),
        rentARoom = Some(UkPropertyExpensesRentARoom(
          amountClaimed = Some(945.66)
        )),
        consolidatedExpenses = None
      ))
    ))
  )

  private val request: AmendUkPropertyPeriodSummaryRequest = AmendUkPropertyPeriodSummaryRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    businessId = businessId,
    submissionId = submissionId,
    body = requestBody
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: AmendUkPropertyPeriodSummaryConnector = new AmendUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AmendUkPropertyPeriodSummaryConnector" must {
    "send a request and return 204 no content" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/business/property/periodic?" +
            s"taxableEntityId=$nino&taxYear=$taxYear&incomeSourceId=$businessId&submissionId=$submissionId",
          config = dummyHeaderCarrierConfig,
          body = requestBody,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        ).returns(Future.successful(outcome))

      await(connector.amendUkPropertyPeriodSummary(request)) shouldBe outcome
    }
  }
}
