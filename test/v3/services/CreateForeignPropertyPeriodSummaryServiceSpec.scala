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

package v3.services

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v3.connectors.MockCreateForeignPropertyPeriodSummaryConnector
import v3.fixtures.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryFixtures
import v3.models.request.createForeignPropertyPeriodSummary._
import v3.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryServiceSpec
    extends ServiceSpec
    with MockCreateForeignPropertyPeriodSummaryConnector
    with CreateForeignPropertyPeriodSummaryFixtures {

  implicit private val correlationId: String          = "X-123"
  implicit private val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2020-21")

  private val requestData = CreateForeignPropertyPeriodSummaryRequestData(nino, businessId, taxYear, regularExpensesRequestBody)

  private val responseData = CreateForeignPropertyPeriodSummaryResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val service = new CreateForeignPropertyPeriodSummaryService(mockCreateForeignPropertyConnector)

  "service" should {
    "service call successful" when {
      "return mapped result" in {
        MockCreateForeignPropertyConnector
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        val result = await(service.createForeignProperty(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in {

          MockCreateForeignPropertyConnector
            .createForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result = await(service.createForeignProperty(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
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
        "PERIOD_OVERLAPS"         -> RuleOverlappingPeriodError,
        "SUBMISSION_DATE_ISSUE"   -> RuleMisalignedPeriodError
      )

      (errors ++ extraTysErrors).foreach((serviceError _).tupled)
    }
  }

}
