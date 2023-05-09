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

package v1.connectors

import api.models.outcomes.ResponseWrapper
import mocks.{MockAppConfig, MockHttpClient}
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.models.request.amendForeignPropertyAnnualSubmission._
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea._
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.Future

class AmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String       = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String    = "2020-21"

  private val foreignFhlEea = ForeignFhlEea(
    Some(
      ForeignFhlEeaAdjustments(
        Some(5000.99),
        Some(5000.99),
        Some(true)
      )),
    Some(
      ForeignFhlEeaAllowances(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ))
  )

  private val foreignPropertyEntry = ForeignPropertyEntry(
    "FRA",
    Some(
      ForeignPropertyAdjustments(
        Some(5000.99),
        Some(5000.99)
      )),
    Some(
      ForeignPropertyAllowances(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(4000.99),
        Some(5000.99)
      ))
  )

  val body: AmendForeignPropertyAnnualSubmissionRequestBody = AmendForeignPropertyAnnualSubmissionRequestBody(
    Some(foreignFhlEea),
    Some(Seq(foreignPropertyEntry))
  )

  val request: AmendForeignPropertyAnnualSubmissionRequest = AmendForeignPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear,
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector = new AmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    "put a body and return a 204" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/business/property/annual/$nino/$businessId/$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amendForeignPropertyAnnualSubmission(request)) shouldBe outcome
    }
  }

}
