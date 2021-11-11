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
import v2.models.request.amendForeignPropertyAnnualSubmission._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.Future

class CreateAmendUKPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2022-23"

  private val ukFhlProperty = ukFhlProperty(
    Some(ukFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      true,
      Some(rentARoom(
        true
      ))
    )),
    Some(ukFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ))
  )

  private val ukNonFhlProperty = ukNonFhlProperty(
    Some(ukNonFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      true,
      Some(rentARoom(
        true
      ))
    )),
    Some(ukNonFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(Seq(structuredBuildingAllowance(
        5000.99,
        Some(firstYear(
          "2020-01-01",
          5000.99
        )),
        Some(building(
          Some("Green Oak's"),
          Some("16AD"),
          Some("GF49JH")
        ))
      )))
    ))
  )

  val body: AmendUKPropertyAnnualSubmissionRequestBody = AmendUKPropertyAnnualSubmissionRequestBody(
    Some(ukFhlProperty),
    Some(ukNonFhlProperty)
  )

  val request: AmendUKPropertyAnnualSubmissionRequest = AmendUKPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear,
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new AmendUKPropertyAnnualSubmissionConnector(
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

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockHttpClient
        .put(
          url = s"$baseUrl/income-tax/business/property/uk/annual/$nino/$businessId/$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amendUKPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}