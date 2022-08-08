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
import v2.models.domain.Nino
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukNonFhlProperty.UkNonFhlProperty
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String = "TC663795B"
  val taxYear: String = "2022-23"
  val businessId: String = "XAIS12345678910"

  val regularExpensesBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody = CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2019-03-11",
    "2020-04-23",
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(42.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some( 31.44),
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
        Some(UkPropertyExpensesRentARoom(
          None,
        )),
        Some(988.18)
      ))
    ))
  ),

  val consolidatedExpensesBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody = CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
    "2019-03-11",
    "2020-04-23",
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(42.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some( 31.44),
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
        Some(UkPropertyExpensesRentARoom(
          None,
        )),
        consolidatedExpenses = Some(988.18)
      ))
    ))
  )

  private val response = CreateHistoricNonFhlUkPropertyPeriodSummaryResponse ("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val regularExpensesRequestData = CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(Nino(nino), regularExpensesBody)

  private val consolidatedExpensesRequestData = CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(Nino(nino), consolidatedExpensesBody)

  class Test extends MockHttpClient with MockAppConfig {
    val connector: CreateHistoricNonFhlUkPropertyPeriodSummaryConnector = new CreateHistoricNonFhlUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDownstreamHeaders)
  }

  "connector" must {
    "post a body with regular expenses and return 200 with submissionId" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=$taxYear&incomeSourceId=$businessId",
          config = dummyIfsHeaderCarrierConfig,
          body = regularExpensesBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.CreateHistoricNonFhlUkProperty(regularExpensesRequestData)) shouldBe outcome

    }

    "post a body with consolidated expenses and return 200 with submissionId" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=$taxYear&incomeSourceId=$businessId",
          config = dummyIfsHeaderCarrierConfig,
          body = consolidatedExpensesBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.CreateHistoricNonFhlUkProperty(consolidatedExpensesRequestData)) shouldBe outcome

    }
  }

