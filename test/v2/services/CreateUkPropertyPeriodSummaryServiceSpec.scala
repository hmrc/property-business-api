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

package v2.services

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateUkPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.request.common.ukFhlProperty._
import v2.models.request.common.ukNonFhlProperty._
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String = "AA123456A"
  val taxYear: String = "2021-22"
  implicit val correlationId: String = "X-123"

  private val regularExpensesBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(UkFhlProperty(
      Some(UkFhlPropertyIncome(
        Some(5000.99),
        Some(3123.21),
        Some(UkPropertyIncomeRentARoom(
          Some(532.12)
        ))
      )),
      Some(UkFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.52),
        consolidatedExpense = None,
        Some(974.47),
        Some(UkPropertyExpensesRentARoom(
          Some(8842.43)
        ))
      ))
    )),
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
        Some(UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
      )),
      Some(UkNonFhlPropertyExpenses(
        Some(3123.21),
        Some(928.42),
        Some(842.99),
        Some(8831.12),
        Some(484.12),
        Some(99282.00),
        Some(999.99),
        Some(974.47),
        Some(8831.12),
        Some(UkPropertyExpensesRentARoom(
          Some(947.66)
        )),
        consolidatedExpense = None
      ))
    ))
  )

  private val consolidatedExpensesBody = CreateUkPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(UkFhlProperty(
      Some(UkFhlPropertyIncome(
        Some(5000.99),
        Some(3123.21),
        Some(UkPropertyIncomeRentARoom(
          Some(532.12)
        ))
      )),
      Some(UkFhlPropertyExpenses(
        None,
        None,
        None,
        None,
        None,
        None,
        consolidatedExpense = Some(41.12),
        None,
        None
      ))
    )),
    Some(UkNonFhlProperty(
      Some(UkNonFhlPropertyIncome(
        Some(41.12),
        Some(84.31),
        Some(9884.93),
        Some(842.99),
        Some(31.44),
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
        residentialFinancialCost = Some(999.99),
        None,
        residentialFinancialCostsCarriedForward = Some(8831.12),
        None,
        consolidatedExpense = Some(947.66)
      ))
    ))
  )

  val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val regularExpensesRequestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, regularExpensesBody)

  private val consolidatedExpensesRequestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, consolidatedExpensesBody)

  trait Test extends MockCreateUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateUkPropertyPeriodSummaryService(
      connector = mockCreateUkPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result for regular Expenses" in new Test {
        MockCreateUkPropertyConnector.createUkProperty(regularExpensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createUkProperty(regularExpensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
      "return mapped result for consolidated Expenses" in new Test {
        MockCreateUkPropertyConnector.createUkProperty(consolidatedExpensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createUkProperty(consolidatedExpensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateUkPropertyConnector.createUkProperty(regularExpensesRequestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.createUkProperty(regularExpensesRequestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_TAX_YEAR_EXPLICIT" -> TaxYearFormatError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_CORRELATIONID" -> DownstreamError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "DUPLICATE_SUBMISSION" -> RuleDuplicateSubmission,
        "NOT_ALIGN_PERIOD" -> RuleMisalignedPeriodError,
        "OVERLAPS_IN_PERIOD" -> RuleOverlappingPeriodError,
        "GAPS_IN_PERIOD" -> RuleNotContiguousPeriodError,
        "INVALID_DATE_RANGE" -> RuleToDateBeforeFromDateError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}