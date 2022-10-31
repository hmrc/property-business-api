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
import v2.models.request.amendUkPropertyAnnualSubmission._
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty._
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty._
import v2.models.request.common.{Building, FirstYear, StructuredBuildingAllowance}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2022-23"

  private val ukFhlProperty = UkFhlProperty(
    Some(UkFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      true,
      Some(5000.99),
      true,
      Some(UkPropertyAdjustmentsRentARoom(true))
    )),
    Some(UkFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      None
    ))
  )

  private val ukNonFhlProperty = UkNonFhlProperty(
    Some(UkNonFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      nonResidentLandlord = true,
      Some(UkPropertyAdjustmentsRentARoom(true))
    )),
    Some(UkNonFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      None,
      Some(Seq(StructuredBuildingAllowance(
        5000.99,
        Some(FirstYear(
          "2020-01-01",
          5000.99
        )),
        Building(
          Some("Green Oak's"),
          None,
          "GF49JH"
        )
      ))),
      Some(Seq(StructuredBuildingAllowance(
        3000.50,
        Some(FirstYear(
          "2020-01-01",
          3000.60
        )),
        Building(
          None,
          Some("house number"),
          "GF49JH"
        )
      )))
    ))
  )

  val body: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(
    Some(ukFhlProperty),
    Some(ukNonFhlProperty)
  )

  val request: AmendUkPropertyAnnualSubmissionRequest = AmendUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear,
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new AmendUkPropertyAnnualSubmissionConnector(
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
          url = s"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=$taxYear",
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amendUkPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}
