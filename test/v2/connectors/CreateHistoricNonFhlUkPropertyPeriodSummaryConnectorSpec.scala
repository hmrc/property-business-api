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
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary._
import v2.models.response.createHistoricNonFhlUkPiePeriodSummary.CreateHistoricNonFhlUkPiePeriodSummaryResponse

import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val transactionReference: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val nino: String                 = "TC663795B"
  val fromDate: String             = "2019-03-11"
  val toDate: String               = "2020-04-23"

  val income: UkNonFhlPropertyIncome =
    UkNonFhlPropertyIncome(Some(2355.45), Some(454.56), Some(123.45), Some(234.53), Some(567.89), Some(UkPropertyIncomeRentARoom(Some(567.56))))

  val expenses: UkNonFhlPropertyExpenses = UkNonFhlPropertyExpenses(
    Some(567.53),
    Some(324.65),
    Some(453.56),
    Some(535.78),
    Some(678.34),
    Some(682.34),
    Some(1000.45),
    Some(645.56),
    Some(672.34),
    Some(
      UkPropertyExpensesRentARoom(
        Some(545.9)
      )
    ),
    None
  )

  val consolidatedExpenses: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(235.78))

  val url: String = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/periodic-summaries"

  val requestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      fromDate,
      toDate,
      Some(income),
      Some(expenses)
    )

  val consolidatedRequestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      fromDate,
      toDate,
      Some(income),
      Some(consolidatedExpenses)
    )

  val requestData: CreateHistoricNonFhlUkPropertyPeriodSummaryRequest =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), requestBody)

  val consolidatedRequestData: CreateHistoricNonFhlUkPropertyPeriodSummaryRequest =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), consolidatedRequestBody)

  val responseData: CreateHistoricNonFhlUkPiePeriodSummaryResponse = CreateHistoricNonFhlUkPiePeriodSummaryResponse(transactionReference)

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

    "post a body with dates, income and expenses and return a 202 with transactionReference" in new Test {
      val outcome = Right(ResponseWrapper(transactionReference, responseData))

      implicit val hc: HeaderCarrier                    = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = url,
          config = dummyIfsHeaderCarrierConfig,
          body = requestBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("Some-Header" -> "some-value")
        )
        .returns(Future.successful(outcome))

      await(connector.createPeriodSummary(requestData)) shouldBe outcome
    }

    "post a body with dates, income and consolidated expenses and return a 202 with transactionReference" in new Test {
      val outcome = Right(ResponseWrapper(transactionReference, responseData))

      implicit val hc: HeaderCarrier                    = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = url,
          config = dummyIfsHeaderCarrierConfig,
          body = consolidatedRequestBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("Some-Header" -> "some-value")
        )
        .returns(Future.successful(outcome))

      await(connector.createPeriodSummary(consolidatedRequestData)) shouldBe outcome
    }
  }

}
