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
import v2.mocks.connectors.MockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{ UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom }
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequest,
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  UkNonFhlPropertyExpenses,
  UkNonFhlPropertyIncome
}

import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  implicit val transactionReference: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val nino: String                          = "TC663795B"
  val fromDate: String                      = "2019-03-11"
  val toDate: String                        = "2020-04-23"

  val url: String = s"/income-tax/nino/$nino/uk-properties/other/periodic-summaries"

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
    Some(UkPropertyExpensesRentARoom(Some(545.9))),
    None
  )

  val consolidatedExpenses: UkNonFhlPropertyExpenses =
    UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(235.78))

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

  private val requestData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), requestBody)

  private val consolidatedRequestData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), consolidatedRequestBody)

  private val responseData = CreateHistoricNonFhlUkPropertyPeriodSummaryResponse(transactionReference)

  trait Test extends MockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateHistoricNonFhlUkPropertyPeriodSummaryService(
      connector = mockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector
    )
  }

  "service" when {
    "service call successful" should {
      "return mapped result for regular period summary" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector
          .createHistoricNonFhlUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(transactionReference, responseData))))

        await(service.createHistoricNonFhlUkProperty(requestData)) shouldBe Right(ResponseWrapper(transactionReference, responseData))
      }

      "return mapped result for consolidated expenses period summary" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector
          .createHistoricNonFhlUkProperty(consolidatedRequestData)
          .returns(Future.successful(Right(ResponseWrapper(transactionReference, responseData))))

        await(service.createHistoricNonFhlUkProperty(consolidatedRequestData)) shouldBe Right(ResponseWrapper(transactionReference, responseData))

      }
    }

    "service call unsuccessful map" when {
      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s" return a$ifsErrorCode from the service" in new Test {
          MockCreateHistoricNonFhlUkPropertyPeriodSummaryConnector
            .createHistoricNonFhlUkProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(transactionReference, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.createHistoricNonFhlUkProperty(requestData)) shouldBe Left(ErrorWrapper(transactionReference, error))
        }

      val input = Seq(
        "INVALID_NINO"            -> NinoFormatError,
        "INVALID_TYPE"            -> InternalError,
        "INVALID_PAYLOAD"         -> InternalError,
        "INVALID_CORRELATIONID"   -> InternalError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "DUPLICATE_SUBMISSION"    -> RuleDuplicateSubmissionError,
        "NOT_ALIGN_PERIOD"        -> RuleMisalignedPeriodError,
        "OVERLAPS_IN_PERIOD"      -> RuleOverlappingPeriodError,
        "NOT_CONTIGUOUS_PERIOD"   -> RuleNotContiguousPeriodError,
        "INVALID_PERIOD"          -> RuleToDateBeforeFromDateError,
        "BOTH_EXPENSES_SUPPLIED"  -> RuleBothExpensesSuppliedError,
        "TAX_YEAR_NOT_SUPPORTED"  -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR"            -> InternalError,
        "SERVICE_UNAVAILABLE"     -> InternalError
      )
      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
