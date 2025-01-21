/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createAmendUkPropertyCumulativeSummary

import common.models.errors._
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v6.createAmendUkPropertyCumulativeSummary.def1.model.request._
import v6.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData

import scala.concurrent.Future

class CreateAmendUkPropertyCumulativeSummaryServiceSpec extends ServiceSpec with MockCreateAmendUkPropertyCumulativeSummaryConnector {

  private val nino                            = "AA123456A"
  private val taxYear                         = "2020-21"
  private val businessId                      = "XAIS12345678910"
  implicit override val correlationId: String = "X-123"

  val requestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
    Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
      fromDate = Some("2023-04-01"),
      toDate = Some("2024-04-01"),
      ukProperty = UkProperty(
        income = Some(
          Income(
            premiumsOfLeaseGrant = Some(42.12),
            reversePremiums = Some(84.31),
            periodAmount = Some(9884.93),
            taxDeducted = Some(842.99),
            otherIncome = Some(31.44),
            rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
          )
        ),
        expenses = Some(
          Expenses(
            premisesRunningCosts = Some(1500.50),
            repairsAndMaintenance = Some(1200.75),
            financialCosts = Some(2000.00),
            professionalFees = Some(500.00),
            costOfServices = Some(300.25),
            other = Some(100.50),
            residentialFinancialCost = Some(9000.10),
            travelCosts = Some(400.00),
            residentialFinancialCostsCarriedForward = Some(300.13),
            rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
            consolidatedExpenses = None
          )
        )
      )
    )

  "CreateAmendUkPropertyCumulativeSummaryService" when {
    "downstream call is successful" when {
      "a submission id is returned from downstream" must {
        "return a successful result" in new Test {
          MockedCreateUkPropertyCumulativeSummaryConnector.createAmendUkPropertyCumulativeSummary(requestData) returns
            Future.successful(Right(ResponseWrapper(correlationId, ())))

          await(service.createAmendUkPropertyCumulativeSummary(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
        }
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in new Test {
            MockedCreateUkPropertyCumulativeSummaryConnector.createAmendUkPropertyCumulativeSummary(requestData) returns
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))

            await(service.createAmendUkPropertyCumulativeSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap = List(
          "INVALID_TAXABLE_ENTITY_ID"          -> NinoFormatError,
          "INVALID_INCOME_SOURCE_ID"           -> BusinessIdFormatError,
          "INVALID_PAYLOAD"                    -> InternalError,
          "INVALID_CORRELATION_ID"             -> InternalError,
          "INVALID_TAX_YEAR"                   -> InternalError,
          "INCOME_SOURCE_NOT_FOUND"            -> NotFoundError,
          "MISSING_EXPENSES"                   -> InternalError,
          "INVALID_SUBMISSION_END_DATE"        -> RuleAdvanceSubmissionRequiresPeriodEndDateError,
          "SUBMISSION_END_DATE_VALUE"          -> RuleSubmissionEndDateCannotMoveBackwardsError,
          "INVALID_START_DATE"                 -> RuleStartDateNotAlignedWithReportingTypeError,
          "START_DATE_NOT_ALIGNED"             -> RuleStartDateNotAlignedToCommencementDateError,
          "END_DATE_NOT_ALIGNED"               -> RuleEndDateNotAlignedWithReportingTypeError,
          "MISSING_SUBMISSION_DATES"           -> RuleMissingSubmissionDatesError,
          "START_END_DATE_NOT_ACCEPTED"        -> RuleStartAndEndDateNotAllowedError,
          "OUTSIDE_AMENDMENT_WINDOW"           -> RuleOutsideAmendmentWindowError,
          "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError,
          "SUBMITTED_TAX_YEAR_NOT_SUPPORTED"   -> RuleTaxYearNotSupportedError,
          "EARLY_DATA_SUBMISSION_NOT_ACCEPTED" -> RuleEarlyDataSubmissionNotAcceptedError,
          "DUPLICATE_COUNTRY_CODE"             -> RuleDuplicateCountryCodeError,
          "SERVER_ERROR"                       -> InternalError,
          "SERVICE_UNAVAILABLE"                -> InternalError
        )

        errorMap.foreach(args => (serviceError _).tupled(args))
      }
    }

    trait Test extends MockCreateAmendUkPropertyCumulativeSummaryConnector {
      val service = new CreateAmendUkPropertyCumulativeSummaryService(mockCreateUkPropertyCumulativeSummaryConnector)

      protected val requestData: CreateAmendUkPropertyCumulativeSummaryRequestData =
        Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), BusinessId(businessId), requestBody)

    }

  }

}
