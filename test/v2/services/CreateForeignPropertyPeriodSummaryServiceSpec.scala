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

package v2.services

import fixtures.CreateForeignPropertyPeriodSummaryFixtures.CreateForeignPropertyPeriodSummaryFixtures
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockCreateForeignPropertyPeriodSummaryConnector
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryServiceSpec extends UnitSpec with CreateForeignPropertyPeriodSummaryFixtures {

  private val businessId: String = "XAIS12345678910"
  private val nino: String       = "AA123456A"
  private val taxYear: TaxYear   = TaxYear.fromMtd("2020-21")

  implicit private val correlationId: String = "X-123"

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockCreateForeignPropertyConnector
          .createForeignProperty(expensesRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createForeignProperty(expensesRequestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockCreateForeignPropertyConnector
            .createForeignProperty(expensesRequestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.createForeignProperty(expensesRequestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "DUPLICATE_COUNTRY_CODE"    -> RuleDuplicateCountryCodeError,
        "INVALID_PAYLOAD"           -> InternalError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "OVERLAPS_IN_PERIOD"        -> RuleOverlappingPeriodError,
        "NOT_ALIGN_PERIOD"          -> RuleMisalignedPeriodError,
        "GAPS_IN_PERIOD"            -> RuleNotContiguousPeriodError,
        "INVALID_DATE_RANGE"        -> RuleToDateBeforeFromDateError,
        "DUPLICATE_SUBMISSION"      -> RuleDuplicateSubmissionError,
        "INCOME_SOURCE_NOT_FOUND"   -> NotFoundError,
        "INCOMPATIBLE_PAYLOAD"      -> RuleTypeOfBusinessIncorrectError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
        "MISSING_EXPENSES"          -> InternalError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = List(
        "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
        "INVALID_CORRELATION_ID"  -> InternalError,
        "PERIOD_NOT_ALIGNED"      -> RuleMisalignedPeriodError,
        "PERIOD_OVERLAPS"         -> RuleOverlappingPeriodError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockCreateForeignPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new CreateForeignPropertyPeriodSummaryService(
      connector = mockCreateForeignPropertyConnector
    )

    protected val response: CreateForeignPropertyPeriodSummaryResponse =
      CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

    protected val expensesRequestData: CreateForeignPropertyPeriodSummaryRequest =
      CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, regularExpensesRequestBody)

  }

}
