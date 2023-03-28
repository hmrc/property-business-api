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

import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockAmendForeignPropertyPeriodSummaryConnector
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._

import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryServiceSpec extends ServiceSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockAmendForeignPropertyPeriodSummaryConnector
          .amendForeignPropertyPeriodSummary(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendForeignPropertyPeriodSummary(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendForeignPropertyPeriodSummaryConnector
              .amendForeignPropertyPeriodSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendForeignPropertyPeriodSummary(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID"   -> NinoFormatError,
          "INVALID_TAX_YEAR"            -> TaxYearFormatError,
          "INVALID_INCOMESOURCEID"      -> BusinessIdFormatError,
          "INVALID_SUBMISSION_ID"       -> SubmissionIdFormatError,
          "INVALID_PAYLOAD"             -> InternalError,
          "INVALID_CORRELATIONID"       -> InternalError,
          "NO_DATA_FOUND"               -> NotFoundError,
          "INCOMPATIBLE_PAYLOAD"        -> RuleTypeOfBusinessIncorrectError,
          "TAX_YEAR_NOT_SUPPORTED"      -> RuleTaxYearNotSupportedError,
          "BUSINESS_VALIDATION_FAILURE" -> InternalError,
          "DUPLICATE_COUNTRY_CODE"      -> RuleDuplicateCountryCodeError,
          "MISSING_EXPENSES"            -> InternalError,
          "SERVER_ERROR"                -> InternalError,
          "SERVICE_UNAVAILABLE"         -> InternalError
        )

        val extraTysErrors = Seq(
          "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"       -> InternalError,
          "INCOME_SOURCE_NOT_COMPATIBLE" -> RuleTypeOfBusinessIncorrectError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  private val foreignFhlEea: AmendForeignFhlEea = AmendForeignFhlEea(
    income = Some(
      ForeignFhlEeaIncome(
        rentAmount = Some(567.83)
      )),
    expenses = Some(
      AmendForeignFhlEeaExpenses(
        premisesRunningCosts = Some(4567.98),
        repairsAndMaintenance = Some(98765.67),
        financialCosts = Some(5000.95),
        professionalFees = Some(23.65),
        costOfServices = Some(4777.77),
        travelCosts = Some(440.88),
        other = Some(569.75),
        consolidatedExpenses = None
      ))
  )

  private val foreignNonFhlPropertyEntry: AmendForeignNonFhlPropertyEntry = AmendForeignNonFhlPropertyEntry(
    countryCode = "FRA",
    income = Some(
      ForeignNonFhlPropertyIncome(
        rentIncome = Some(
          ForeignNonFhlPropertyRentIncome(
            rentAmount = Some(34456.30)
          )),
        foreignTaxCreditRelief = true,
        premiumsOfLeaseGrant = Some(2543.43),
        otherPropertyIncome = Some(54325.30),
        foreignTaxPaidOrDeducted = Some(6543.01),
        specialWithholdingTaxOrUkTaxPaid = Some(643245.00)
      )),
    expenses = Some(
      AmendForeignNonFhlPropertyExpenses(
        premisesRunningCosts = Some(5635.43),
        repairsAndMaintenance = Some(3456.65),
        financialCosts = Some(34532.21),
        professionalFees = Some(32465.32),
        costOfServices = Some(2567.21),
        travelCosts = Some(2345.76),
        residentialFinancialCost = Some(21235.22),
        broughtFwdResidentialFinancialCost = Some(12556.00),
        other = Some(2425.11),
        consolidatedExpenses = None
      ))
  )

  private val requestBody: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignNonFhlProperty = Some(Seq(foreignNonFhlPropertyEntry))
  )

  private val request: AmendForeignPropertyPeriodSummaryRequest = AmendForeignPropertyPeriodSummaryRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = taxYear,
    submissionId = submissionId,
    body = requestBody
  )

  trait Test extends MockAmendForeignPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendForeignPropertyPeriodSummaryService(
      connector = mockAmendForeignPropertyPeriodSummaryConnector
    )
  }
}
