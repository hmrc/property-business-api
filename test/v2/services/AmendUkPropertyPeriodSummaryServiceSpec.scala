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

package v2.services

import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockAmendUkPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyPeriodSummary.{AmendUkPropertyPeriodSummaryRequest, AmendUkPropertyPeriodSummaryRequestBody}
import v2.models.request.common.ukFhlProperty._
import v2.models.request.common.ukNonFhlProperty._
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}

import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  val nino: String = "AA123456A"
  val taxYear: String = "2022-23"
  val businessId: String = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val requestBody: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(
    ukFhlProperty = Some(UkFhlProperty(
      income = Some(UkFhlPropertyIncome(
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
    ukNonFhlProperty = Some(UkNonFhlProperty(
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

  private val request: AmendUkPropertyPeriodSummaryRequest = AmendUkPropertyPeriodSummaryRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    businessId = businessId,
    submissionId = submissionId,
    body = requestBody
  )

  trait Test extends MockAmendUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendUkPropertyPeriodSummaryService(
      connector = mockAmendUkPropertyPeriodSummaryConnector
    )
  }

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendUkPropertyPeriodSummaryConnector
          .amendUkPropertyPeriodSummary(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendUkPropertyPeriodSummary(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockAmendUkPropertyPeriodSummaryConnector
              .amendUkPropertyPeriodSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

            await(service.amendUkPropertyPeriodSummary(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR" -> TaxYearFormatError,
          "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
          "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
          "INVALID_PAYLOAD" -> DownstreamError,
          "INVALID_CORRELATIONID" -> DownstreamError,
          "NO_DATA_FOUND" -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> DownstreamError,
          "DUPLICATE_COUNTRY_CODE" -> DownstreamError,
          "SERVER_ERROR" -> DownstreamError,
          "SERVICE_UNAVAILABLE" -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
