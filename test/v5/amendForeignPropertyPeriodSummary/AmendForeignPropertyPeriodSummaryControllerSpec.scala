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

package v5.amendForeignPropertyPeriodSummary

import common.models.domain.SubmissionId
import common.models.errors.RuleMisalignedPeriodError
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v5.amendForeignPropertyPeriodSummary.def1.model.request.foreignFhlEea.{AmendForeignFhlEea, AmendForeignFhlEeaExpenses, ForeignFhlEeaIncome}
import v5.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry.{
  AmendForeignNonFhlPropertyEntry,
  AmendForeignNonFhlPropertyExpenses,
  ForeignNonFhlPropertyIncome,
  ForeignNonFhlPropertyRentIncome
}
import v5.amendForeignPropertyPeriodSummary.model.request.{
  AmendForeignPropertyPeriodSummaryRequestData,
  Def1_AmendForeignPropertyPeriodSummaryRequestBody,
  Def1_AmendForeignPropertyPeriodSummaryRequestData
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyPeriodSummaryService
    with MockAmendForeignPropertyPeriodSummaryValidatorFactory
    with MockAuditService
    with MockIdGenerator {

  private val businessId                         = "XAIS12345678910"
  private val taxYear                            = "2022-23"
  private val submissionId                       = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "AmendForeignPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = None)
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = None)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)

      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new AmendForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendForeignPropertyPeriodSummaryValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] =
      controller.handleRequest(nino = validNino, businessId = businessId, taxYear = taxYear, submissionId = submissionId)(
        fakePutRequest(requestBodyJson))

    protected val requestBody: Def1_AmendForeignPropertyPeriodSummaryRequestBody =
      Def1_AmendForeignPropertyPeriodSummaryRequestBody(
        Some(
          AmendForeignFhlEea(
            Some(ForeignFhlEeaIncome(Some(5000.99))),
            Some(
              AmendForeignFhlEeaExpenses(
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
        Some(
          List(AmendForeignNonFhlPropertyEntry(
            "FRA",
            Some(
              ForeignNonFhlPropertyIncome(
                Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
                foreignTaxCreditRelief = false,
                Some(5000.99),
                Some(5000.99),
                Some(5000.99),
                Some(5000.99)
              )),
            Some(
              AmendForeignNonFhlPropertyExpenses(
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
              ))
          )))
      )

    protected val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "foreignFhlEea": {
        |    "income": {
        |      "rentAmount": 5000.99
        |    },
        |    "expenses": {
        |      "premisesRunningCosts": 5000.99,
        |      "repairsAndMaintenance": 5000.99,
        |      "financialCosts": 5000.99,
        |      "professionalFees": 5000.99,
        |      "costOfServices": 5000.99,
        |      "travelCosts": 5000.99,
        |      "other": 5000.99,
        |      "consolidatedExpenses": 5000.99
        |    }
        |  },
        |  "foreignNonFhlProperty": [
        |    {
        |      "countryCode": "FRA",
        |      "income": {
        |        "rentIncome": {
        |          "rentAmount": 5000.99
        |        },
        |        "foreignTaxCreditRelief": false,
        |        "premiumsOfLeaseGrant": 5000.99,
        |        "otherPropertyIncome": 5000.99,
        |        "foreignTaxPaidOrDeducted": 5000.99,
        |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
        |      },
        |      "expenses": {
        |        "premisesRunningCosts": 5000.99,
        |        "repairsAndMaintenance": 5000.99,
        |        "financialCosts": 5000.99,
        |        "professionalFees": 5000.99,
        |        "costOfServices": 5000.99,
        |        "travelCosts": 5000.99,
        |        "residentialFinancialCost": 5000.99,
        |        "broughtFwdResidentialFinancialCost": 5000.99,
        |        "other": 5000.99,
        |        "consolidatedExpenses": 5000.99
        |      }
        |    }
        |  ]
        |}
    """.stripMargin
    )

    protected val requestData: AmendForeignPropertyPeriodSummaryRequestData =
      Def1_AmendForeignPropertyPeriodSummaryRequestData(
        Nino(validNino),
        BusinessId(businessId),
        TaxYear.fromMtd(taxYear),
        SubmissionId(submissionId),
        requestBody)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendForeignPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "amend-foreign-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "4.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear, "submissionId" -> submissionId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
