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

package v2.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockHttpClient
import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String = "AA123456A"
  val taxYear: String = "2022-23"

  val regularExpensesBody: CreateUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(UkFhlProperty(
      Some(UkFhlPropertyIncome(
        Some(5000.99),
        Some(3123.21),
        Some(UkPropertyIncomeRentARoom(
          Some(532.12)
        ))
      )),
      Some(UkFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.52),
        consolidatedExpenses = None,
        Some(974.47),
        Some(UkPropertyExpensesRentARoom(
          Some(8842.43)
        ))
      ))
    )),
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
        Some(UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
      )),
      Some(UkNonFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.00),
        Some(999.99),
        Some(974.47),
        Some(8831.12),
        Some(UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
        consolidatedExpenses = None
      ))
    ))
  )

  val consolidatedExpensesBody: CreateUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(UkFhlProperty(
      Some(UkFhlPropertyIncome(
        Some(5000.99),
        Some(3123.21),
        Some(UkPropertyIncomeRentARoom(
          Some(532.12)
        ))
      )),
      Some(UkFhlPropertyExpenses(
        None,
        None,
        None,
        None,
        None,
        None,
        consolidatedExpenses = Some(41.12),
        None,
        None
      ))
    )),
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
        Some(UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
      )),
      Some(UkNonFhlPropertyExpenses(
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        consolidatedExpenses = Some(947.66)
      ))
    ))
  )

  private val response = CreateUkPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val regularExpensesRequestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, regularExpensesBody)

  private val consolidatedExpensesRequestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, consolidatedExpensesBody)

  class Test extends MockHttpClient with MockAppConfig {
    val connector: CreateUkPropertyPeriodSummaryConnector = new CreateUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    "post a body with regular expenses and return 200 with submissionId" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYearExplicit=$taxYear&incomeSourceId=$businessId",
          config = dummyIfsHeaderCarrierConfig,
          body = regularExpensesBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createUkProperty(regularExpensesRequestData)) shouldBe outcome

    }

    "post a body with consolidated expenses and return 200 with submissionId" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYearExplicit=$taxYear&incomeSourceId=$businessId",
          config = dummyIfsHeaderCarrierConfig,
          body = consolidatedExpensesBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createUkProperty(consolidatedExpensesRequestData)) shouldBe outcome

    }
  }
}