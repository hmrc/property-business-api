/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.models.request.createForeignProperty._
import v1.models.response.create.CreateForeignPropertyResponse

import scala.concurrent.Future

class CreateForeignPropertyConnectorSpec extends ConnectorSpec {

  val businessId = "XAIS12345678910"
  val nino = Nino("AA123456A")

  val regularExpensesBody = CreateForeignPropertyRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99, Some(5000.99)),
      Some(ForeignFhlEeaExpenditure(
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
    Some(Seq(ForeignProperty("FRA",
      ForeignPropertyIncome(
        RentIncome(5000.99, 5000.99),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ),
      Some(ForeignPropertyExpenditure(
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

  val consolidatedExpensesBody = CreateForeignPropertyRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99, Some(5000.99)),
      Some(ForeignFhlEeaExpenditure(
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(3653.35)
      ))
    )),
    Some(Seq(ForeignProperty("FRA",
      ForeignPropertyIncome(
        RentIncome(5000.99, 5000.99),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ),
      Some(ForeignPropertyExpenditure(
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(235324.23)
      ))))
    ))

  val response = CreateForeignPropertyResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val regularExpensesRequestData = CreateForeignPropertyRequestData(nino, businessId, regularExpensesBody)

  private val consolidatedExpensesRequestData = CreateForeignPropertyRequestData(nino, businessId, consolidatedExpensesBody)

  class Test extends MockHttpClient with MockAppConfig {
    val connector: CreateForeignPropertyConnector = new CreateForeignPropertyConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "connector" must {
    "put a body with regular expenses and return 200 with submissionId" in new Test {

      val outcome = Right(ResponseWrapper(correlationId, response))
      MockedHttpClient
        .post(
          url = s"$baseUrl/business/property/${nino}/${businessId}/period",
          body = regularExpensesBody,
          requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.createForeignProperty(regularExpensesRequestData)) shouldBe outcome

    }

    "put a body with consolidated expenses and return 200 with submissionId" in new Test {

      val outcome = Right(ResponseWrapper(correlationId, response))
      MockedHttpClient
        .post(
          url = s"$baseUrl/business/property/${nino}/${businessId}/period",
          body = consolidatedExpensesBody,
          requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.createForeignProperty(consolidatedExpensesRequestData)) shouldBe outcome

    }
  }
}
