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
import org.scalamock.handlers.CallHandler
import v2.mocks.MockHttpClient
import v2.models.domain.{Nino, TaxYear}
import v2.models.errors.{DownstreamErrorCode, DownstreamErrors}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.{AnnualAdjustments,
  AnnualAllowances, RentARoom, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse}

import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val mtdTaxYear: String        = "2019-20"
  val downstreamTaxYear: String = "2020"

  val request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest = RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(mtdTaxYear)
  )

  val annualAdjustments: AnnualAdjustments = AnnualAdjustments(
    lossBroughtForward = Some(200.00),
    balancingCharge = Some(200.00),
    privateUseAdjustment = Some(200.00),
    businessPremisesRenovationAllowanceBalancingCharges = Some(200.00),
    nonResidentLandlord = false,
    rentARoom = Some(RentARoom(false)))

  val annualAllowances: AnnualAllowances =
    AnnualAllowances(
      annualInvestmentAllowance = Some(200.00),
      otherCapitalAllowance = Some(200.00),
      zeroEmissionGoodsVehicleAllowance = Some(200.00),
      businessPremisesRenovationAllowance = Some(200.00),
      costOfReplacingDomesticGoods = Some(200.00),
      propertyIncomeAllowance = Some(200.00))

  def responseWith(annualAdjustments: Option[AnnualAdjustments],
                   annualAllowances: Option[AnnualAllowances]): RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
    RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(annualAdjustments, annualAllowances)

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDownstreamHeaders)

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]]]#Derived = {
      MockHttpClient
        .get(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/$downstreamTaxYear",
          config = dummyDesHeaderCarrierConfig,
          requiredHeaders = requiredDesHeaders,
        )
        .returns(Future.successful(outcome))
    }
  }

  "retrieve" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
          responseWith(Some(annualAdjustments), Some(annualAllowances))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return object with only annualAdjustments" when {
      "response received contains only annualAdjustments" in new Test {
        val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse = responseWith(Some(annualAdjustments), None)
        private val outcome  = Right(ResponseWrapper(correlationId, response))
        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return object with only annualAllowances" when {
      "response received contains only annualAllowances" in new Test {
        val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse = responseWith(None, Some(annualAllowances))
        private val outcome  = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return empty object with" must {
      "return a valid result" in new Test {
        val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse = responseWith(None, None)
        private val outcome  = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return an error as per the spec" when {
      "an error response received" in new Test {
        private val outcome = Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
        stubHttpResponse(outcome)

        private val result = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }
}
