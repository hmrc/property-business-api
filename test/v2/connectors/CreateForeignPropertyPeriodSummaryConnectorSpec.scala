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
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"

  private val regularExpensesBody = CreateForeignPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(CreateForeignFhlEea(
      Some(ForeignFhlEeaIncome(Some(5000.99))),
      Some(CreateForeignFhlEeaExpenses(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None
      ))
    )),
    Some(Seq(CreateForeignNonFhlPropertyEntry("FRA",
      Some(ForeignNonFhlPropertyIncome(
        Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
        foreignTaxCreditRelief = false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      )),
      Some(CreateForeignNonFhlPropertyExpenses(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None
      ))))
    ))


  private val response = CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val regularExpensesRequestData = CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, regularExpensesBody)


  class Test extends MockHttpClient with MockAppConfig {
    val connector: CreateForeignPropertyPeriodSummaryConnector = new CreateForeignPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    "post a valid body and return 200 with submissionId" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, response))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .post(
          url = s"$baseUrl/income-tax/business/property/periodic?taxableEntityId=$nino&taxYear=$taxYear&incomeSourceId=$businessId",
          config = dummyHeaderCarrierConfig,
          body = regularExpensesBody,
          requiredHeaders = requiredIfsHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createForeignProperty(regularExpensesRequestData)) shouldBe outcome

    }

  }
}
