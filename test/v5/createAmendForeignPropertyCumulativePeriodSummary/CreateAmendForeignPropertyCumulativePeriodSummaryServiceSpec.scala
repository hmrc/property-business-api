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

package v5.createAmendForeignPropertyCumulativePeriodSummary

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v5.createAmendForeignPropertyCumulativePeriodSummary.def1.model.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures
import v5.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

import scala.concurrent.Future

class CreateAmendForeignPropertyCumulativePeriodSummaryServiceSpec
    extends ServiceSpec
    with MockCreateAmendForeignPropertyCumulativePeriodSummaryConnector
    with Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures {

  implicit private val correlationId: String          = "X-123"
  implicit private val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2025-26")

  private val requestData = Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(nino, businessId, taxYear, regularExpensesRequestBody)

  private val responseData: Unit = ()

  private val service = new CreateAmendForeignPropertyCumulativePeriodSummaryService(mockCreateAmendForeignPropertyCumulativePeriodSummaryConnector)

  "service" should {
    "be successful" when {
      "given a valid request" in {
        MockedCreateAmendForeignPropertyCumulativePeriodSummaryConnector
          .createAmendForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        val result = await(service.createAmendForeignProperty(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseData))
      }
    }
    "be unsuccessful and map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in {

          MockedCreateAmendForeignPropertyCumulativePeriodSummaryConnector
            .createAmendForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result = await(service.createAmendForeignProperty(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAXABLE_ENTITY_ID"          -> NinoFormatError,
        "INVALID_INCOME_SOURCE_ID"           -> BusinessIdFormatError,
        "INVALID_PAYLOAD"                    -> InternalError,
        "INVALID_CORRELATION_ID"             -> InternalError,
        "INVALID_TAX_YEAR"                   -> InternalError,
        "INCOME_SOURCE_NOT_FOUND"            -> NotFoundError,
        "SUBMITTED_TAX_YEAR_NOT_SUPPORTED"   -> RuleTaxYearNotSupportedError,
        "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError,
        "MISSING_EXPENSES"                   -> InternalError,
        "INVALID_SUBMISSION_END_DATE"        -> RuleAdvanceSubmissionRequiresPeriodEndDateError,
        "SUBMISSION_END_DATE_VALUE"          -> RuleSubmissionEndDateCannotMoveBackwardsError,
        "INVALID_START_DATE"                 -> RuleStartDateNotAlignedWithReportingTypeError,
        "START_DATE_NOT_ALIGNED"             -> RuleStartDateNotAlignedToCommencementDateError,
        "END_DATE_NOT_ALIGNED"               -> RuleEndDateNotAlignedWithReportingTypeError,
        "MISSING_SUBMISSION_DATES"           -> InternalError,
        "START_END_DATE_NOT_ACCEPTED"        -> RuleStartAndEndDateNotAllowedError,
        "OUTSIDE_AMENDMENT_WINDOW"           -> RuleOutsideAmendmentWindowError,
        "EARLY_DATA_SUBMISSION_NOT_ACCEPTED" -> RuleEarlyDataSubmissionNotAcceptedError,
        "DUPLICATE_COUNTRY_CODE"             -> RuleDuplicateCountryCodeError,
        "SERVER_ERROR"                       -> InternalError,
        "SERVICE_UNAVAILABLE"                -> InternalError
      )

      errors.foreach((serviceError _).tupled)
    }
  }

}
