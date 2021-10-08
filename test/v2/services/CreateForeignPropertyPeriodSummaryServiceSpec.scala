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
  implicit val correlationId: String = "X-123"

  private val regularExpensesBody = CreateForeignPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99),
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
    Some(Seq(ForeignPropertyEntry("FRA",
      ForeignPropertyIncome(
        ForeignPropertyRentIncome(5000.99),
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

  private val consolidatedExpensesBody = CreateForeignPropertyPeriodSummaryRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99),
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
    Some(Seq(ForeignPropertyEntry("FRA",
      ForeignPropertyIncome(
        ForeignPropertyRentIncome(5000.99),
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

  val response: CreateForeignPropertyPeriodSummaryResponse = CreateForeignPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val regularExpensesRequestData = CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, regularExpensesBody)

  private val consolidatedExpensesRequestData = CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, consolidatedExpensesBody)

  trait Test extends MockCreateForeignPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateForeignPropertyPeriodSummaryService(
      connector = mockCreateForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result for regular Expenses" in new Test {
        MockCreateForeignPropertyConnector.createForeignProperty(regularExpensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createForeignProperty(regularExpensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
      "return mapped result for consolidated Expenses" in new Test {
        MockCreateForeignPropertyConnector.createForeignProperty(consolidatedExpensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createForeignProperty(consolidatedExpensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateForeignPropertyConnector.createForeignProperty(regularExpensesRequestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.createForeignProperty(regularExpensesRequestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_CORRELATIONID" -> DownstreamError,
        "OVERLAPS_IN_PERIOD" -> RuleOverlappingPeriodError,
        "NOT_ALIGN_PERIOD" -> RuleMisalignedPeriodError,
        "GAPS_IN_PERIOD" -> RuleNotContiguousPeriodError,
        "INVALID_DATE_RANGE" -> RuleToDateBeforeFromDateError,
        "DUPLICATE_SUBMISSION" -> RuleDuplicateSubmission,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}