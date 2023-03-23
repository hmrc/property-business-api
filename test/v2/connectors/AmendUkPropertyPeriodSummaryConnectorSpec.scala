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

package v2.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import org.scalamock.handlers.CallHandler
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.ResponseWrapper
import v2.models.request.amendUkPropertyPeriodSummary.{AmendUkPropertyPeriodSummaryRequest, AmendUkPropertyPeriodSummaryRequestBody}
import v2.models.request.common.ukFhlProperty._
import v2.models.request.common.ukNonFhlProperty._
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryConnectorSpec extends ConnectorSpec {

  val nino: String         = "AA123456A"
  val businessId: String   = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "AmendUkPropertyPeriodSummaryConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))

    "amendUkPropertyPeriodSummary" must {
      "send a request and return 204 no content" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        await(connector.amendUkPropertyPeriodSummary(request)) shouldBe outcome
      }
    }

    "amendUkPropertyPeriodSummary is called with a TYS tax year" must {
      "send a request and return 204 no content" in new TysIfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        await(connector.amendUkPropertyPeriodSummary(request)) shouldBe outcome
      }
    }

    "response is an error" must {

      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear
        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyPeriodSummary(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new TysIfsTest with Test {
        def taxYear: TaxYear = tysTaxYear
        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyPeriodSummary(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    val connector: AmendUkPropertyPeriodSummaryConnector = new AmendUkPropertyPeriodSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: AmendUkPropertyPeriodSummaryRequest = AmendUkPropertyPeriodSummaryRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      businessId = businessId,
      submissionId = submissionId,
      body = requestBody
    )

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/periodic?" +
          s"taxableEntityId=$nino&taxYear=${taxYear.asMtd}&incomeSourceId=$businessId&submissionId=$submissionId",
        body = requestBody,
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/periodic/${taxYear.asTysDownstream}?" +
          s"taxableEntityId=$nino&incomeSourceId=$businessId&submissionId=$submissionId",
        body = requestBody,
      ).returns(Future.successful(outcome))
    }
  }

  private val requestBody: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(
    ukFhlProperty = Some(
      UkFhlProperty(
        income = Some(
          UkFhlPropertyIncome(
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
    ukNonFhlProperty = Some(
      UkNonFhlProperty(
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

}
