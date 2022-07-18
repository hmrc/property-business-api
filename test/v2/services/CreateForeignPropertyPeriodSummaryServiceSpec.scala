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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateForeignPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val businessId: String = "XAIS12345678910"
  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  implicit val correlationId: String = "X-123"

  private val expensesBody = CreateForeignPropertyPeriodSummaryRequestBody(
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


  val response: CreateForeignPropertyPeriodSummaryResponse = CreateForeignPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val expensesRequestData = CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, expensesBody)


  trait Test extends MockCreateForeignPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateForeignPropertyPeriodSummaryService(
      connector = mockCreateForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockCreateForeignPropertyConnector.createForeignProperty(expensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createForeignProperty(expensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateForeignPropertyConnector.createForeignProperty(expensesRequestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.createForeignProperty(expensesRequestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "DUPLICATE_COUNTRY_CODE" -> RuleDuplicateCountryCodeError,
        "INVALID_PAYLOAD" -> DownstreamMtdError,
        "INVALID_CORRELATIONID" -> DownstreamMtdError,
        "OVERLAPS_IN_PERIOD" -> RuleOverlappingPeriodError,
        "NOT_ALIGN_PERIOD" -> RuleMisalignedPeriodError,
        "GAPS_IN_PERIOD" -> RuleNotContiguousPeriodError,
        "INVALID_DATE_RANGE" -> RuleToDateBeforeFromDateError,
        "DUPLICATE_SUBMISSION" -> RuleDuplicateSubmissionError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrectError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "MISSING_EXPENSES" -> DownstreamMtdError,
        "SERVER_ERROR" -> DownstreamMtdError,
        "SERVICE_UNAVAILABLE" -> DownstreamMtdError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
