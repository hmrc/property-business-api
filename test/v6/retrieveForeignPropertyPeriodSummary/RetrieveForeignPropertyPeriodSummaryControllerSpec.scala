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

package v6.retrieveForeignPropertyPeriodSummary

import common.models.domain.SubmissionId
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain._
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenses, ForeignFhlEeaIncome}
import v6.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignNonFhlProperty._
import v6.retrieveForeignPropertyPeriodSummary.model.request.{
  Def1_RetrieveForeignPropertyPeriodSummaryRequestData,
  RetrieveForeignPropertyPeriodSummaryRequestData
}
import v6.retrieveForeignPropertyPeriodSummary.model.response.{
  Def1_RetrieveForeignPropertyPeriodSummaryResponse,
  RetrieveForeignPropertyPeriodSummaryResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyPeriodSummaryService
    with MockRetrieveForeignPropertyPeriodSummaryValidatorFactory
    with MockIdGenerator {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2022-23"

  "RetrieveForeignPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        val expectedResponseBody: JsValue = Json.toJson(responseBody)
        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(expectedResponseBody))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(expectedError = NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(expectedError = RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveForeignPropertyService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] =
      controller.handleRequest(nino = validNino, businessId = businessId, taxYear = taxYear, submissionId = submissionId)(fakeGetRequest)

    protected val requestData: RetrieveForeignPropertyPeriodSummaryRequestData =
      Def1_RetrieveForeignPropertyPeriodSummaryRequestData(
        Nino(validNino),
        BusinessId(businessId),
        TaxYear.fromMtd(taxYear),
        SubmissionId(submissionId))

    protected val responseBody: RetrieveForeignPropertyPeriodSummaryResponse = Def1_RetrieveForeignPropertyPeriodSummaryResponse(
      submittedOn = Timestamp("2022-06-17T10:53:38Z"),
      fromDate = "",
      toDate = "",
      foreignFhlEea = Some(
        ForeignFhlEea(
          income = Some(
            ForeignFhlEeaIncome(
              rentAmount = Some(3426.34)
            )),
          expenses = Some(ForeignFhlEeaExpenses(
            premisesRunningCosts = Some(1000.12),
            repairsAndMaintenance = Some(1000.12),
            financialCosts = Some(1000.12),
            professionalFees = Some(1000.12),
            costOfServices = Some(1000.12),
            travelCosts = Some(1000.12),
            other = Some(1000.12),
            consolidatedExpenses = None
          ))
        )),
      foreignNonFhlProperty = Some(
        List(
          ForeignNonFhlProperty(
            countryCode = "ZZZ",
            income = Some(
              ForeignNonFhlPropertyIncome(
                rentIncome = Some(
                  ForeignNonFhlPropertyRentIncome(
                    rentAmount = Some(1000.12)
                  )),
                foreignTaxCreditRelief = true,
                premiumsOfLeaseGrant = Some(1000.12),
                otherPropertyIncome = Some(1000.12),
                foreignTaxPaidOrDeducted = Some(1000.12),
                specialWithholdingTaxOrUkTaxPaid = Some(1000.12)
              )),
            expenses = Some(
              ForeignNonFhlPropertyExpenses(
                premisesRunningCosts = Some(1000.12),
                repairsAndMaintenance = Some(1000.12),
                financialCosts = Some(1000.12),
                professionalFees = Some(1000.12),
                costOfServices = Some(1000.12),
                travelCosts = Some(1000.12),
                residentialFinancialCost = Some(1000.12),
                broughtFwdResidentialFinancialCost = Some(1000.12),
                other = Some(1000.12),
                consolidatedExpenses = None
              ))
          )
        )
      )
    )

  }

}
