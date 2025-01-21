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

package v5.amendUkPropertyPeriodSummary

import common.models.domain.SubmissionId
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v5.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty._
import v5.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty._
import v5.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom._
import v5.amendUkPropertyPeriodSummary.model.request._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendUkPropertyPeriodSummaryService
    with MockAmendUkPropertyPeriodSummaryValidatorFactory
    with MockAuditService {

  private val businessId                         = "XAIS12345678910"
  private val taxYear                            = "2020-21"
  private val submissionId                       = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "AmendUkPropertyPeriodSummaryController" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestDataConsolidatedExpenses))

        MockAmendUkPropertyService
          .amend(requestDataConsolidatedExpenses)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        override def callController(): Future[Result] =
          controller.handleRequest(validNino, businessId, taxYear, submissionId)(fakePutRequest(requestBodyJsonConsolidatedExpense))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJsonConsolidatedExpense),
          maybeExpectedResponseBody = None
        )
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendUkPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendUkPropertyService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new AmendUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendUkPropertyPeriodSummaryValidatorFactory,
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
      controller.handleRequest(validNino, businessId, taxYear, submissionId)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendUKPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "amend-uk-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "9.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear, "submissionId" -> submissionId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyWithoutConsolidatedExpenses: Def1_AmendUkPropertyPeriodSummaryRequestBody = {
      Def1_AmendUkPropertyPeriodSummaryRequestBody(
        ukFhlProperty = Some(
          Def1_Amend_UkFhlProperty(
            income = Some(
              Def1_Amend_UkFhlPropertyIncome(
                periodAmount = Some(5000.99),
                taxDeducted = Some(3123.21),
                rentARoom = Some(Def1_Amend_UkPropertyIncomeRentARoom(rentsReceived = Some(532.12))))),
            expenses = Some(Def1_Amend_UkFhlPropertyExpenses(
              premisesRunningCosts = Some(3123.21),
              repairsAndMaintenance = Some(928.42),
              financialCosts = Some(842.99),
              professionalFees = Some(8831.12),
              costOfServices = Some(484.12),
              other = Some(99282),
              consolidatedExpenses = None,
              travelCosts = Some(974.47),
              rentARoom = Some(Def1_Amend_UkPropertyExpensesRentARoom(amountClaimed = Some(8842.43)))
            ))
          )),
        ukNonFhlProperty = Some(
          Def1_Amend_UkNonFhlProperty(
            income = Some(Def1_Amend_UkNonFhlPropertyIncome(
              premiumsOfLeaseGrant = Some(41.12),
              reversePremiums = Some(84.31),
              periodAmount = Some(9884.93),
              taxDeducted = Some(842.99),
              otherIncome = Some(31.44),
              rentARoom = Some(Def1_Amend_UkPropertyIncomeRentARoom(rentsReceived = Some(947.66)))
            )),
            expenses = Some(Def1_Amend_UkNonFhlPropertyExpenses(
              premisesRunningCosts = None,
              repairsAndMaintenance = None,
              financialCosts = None,
              professionalFees = None,
              costOfServices = None,
              other = None,
              residentialFinancialCost = None,
              travelCosts = None,
              residentialFinancialCostsCarriedForward = None,
              rentARoom = None,
              consolidatedExpenses = Some(988.18)
            ))
          ))
      )

    }

    private val requestBodyWithConsolidatedExpenses: Def1_AmendUkPropertyPeriodSummaryRequestBody = {
      Def1_AmendUkPropertyPeriodSummaryRequestBody(
        ukFhlProperty = Some(
          Def1_Amend_UkFhlProperty(
            income = Some(
              Def1_Amend_UkFhlPropertyIncome(
                periodAmount = Some(5000.99),
                taxDeducted = Some(3123.21),
                rentARoom = Some(Def1_Amend_UkPropertyIncomeRentARoom(rentsReceived = Some(532.12))))),
            expenses = Some(Def1_Amend_UkFhlPropertyExpenses(
              premisesRunningCosts = None,
              repairsAndMaintenance = None,
              financialCosts = None,
              professionalFees = None,
              costOfServices = None,
              other = None,
              consolidatedExpenses = Some(988.18),
              travelCosts = None,
              rentARoom = None
            ))
          )),
        ukNonFhlProperty = Some(
          Def1_Amend_UkNonFhlProperty(
            income = Some(Def1_Amend_UkNonFhlPropertyIncome(
              premiumsOfLeaseGrant = Some(41.12),
              reversePremiums = Some(84.31),
              periodAmount = Some(9884.93),
              taxDeducted = Some(842.99),
              otherIncome = Some(31.44),
              rentARoom = Some(Def1_Amend_UkPropertyIncomeRentARoom(rentsReceived = Some(947.66)))
            )),
            expenses = Some(Def1_Amend_UkNonFhlPropertyExpenses(
              premisesRunningCosts = None,
              repairsAndMaintenance = None,
              financialCosts = None,
              professionalFees = None,
              costOfServices = None,
              other = None,
              residentialFinancialCost = None,
              travelCosts = None,
              residentialFinancialCostsCarriedForward = None,
              rentARoom = None,
              consolidatedExpenses = Some(988.18)
            ))
          ))
      )

    }

    protected val requestBodyJson: JsValue = Json.parse(
      """{
        |    "ukFhlProperty":{
        |        "income": {
        |            "periodAmount": 5000.99,
        |            "taxDeducted": 3123.21,
        |            "rentARoom": {
        |                "rentsReceived": 532.12
        |            }
        |        },
        |        "expenses": {
        |            "premisesRunningCosts": 3123.21,
        |            "repairsAndMaintenance": 928.42,
        |            "financialCosts": 842.99,
        |            "professionalFees": 8831.12,
        |            "costOfServices": 484.12,
        |            "other": 99282,
        |            "travelCosts": 974.47,
        |            "rentARoom": {
        |                "amountClaimed": 8842.43
        |            }
        |        }
        |    },
        |    "ukNonFhlProperty": {
        |        "income": {
        |            "premiumsOfLeaseGrant": 42.12,
        |            "reversePremiums": 84.31,
        |            "periodAmount": 9884.93,
        |            "taxDeducted": 842.99,
        |            "otherIncome": 31.44,
        |            "rentARoom": {
        |                "rentsReceived": 947.66
        |            }
        |        },
        |        "expenses": {
        |            "premisesRunningCosts": 3123.21,
        |            "repairsAndMaintenance": 928.42,
        |            "financialCosts": 842.99,
        |            "professionalFees": 8831.12,
        |            "costOfServices": 484.12,
        |            "other": 99282,
        |            "residentialFinancialCost": 12.34,
        |            "travelCosts": 974.47,
        |            "residentialFinancialCostsCarriedForward": 12.34,
        |            "rentARoom": {
        |                "amountClaimed": 8842.43
        |            }
        |        }
        |    }
        |}
        |""".stripMargin
    )

    protected val requestBodyJsonConsolidatedExpense: JsValue = Json.parse(
      """{
        |    "ukFhlProperty":{
        |        "income": {
        |            "periodAmount": 5000.99,
        |            "taxDeducted": 3123.21,
        |            "rentARoom": {
        |                "rentsReceived": 532.12
        |            }
        |        },
        |        "expenses": {
        |            "consolidatedExpense": 988.18
        |        }
        |    },
        |    "ukNonFhlProperty": {
        |        "income": {
        |            "premiumsOfLeaseGrant": 42.12,
        |            "reversePremiums": 84.31,
        |            "periodAmount": 9884.93,
        |            "taxDeducted": 842.99,
        |            "otherIncome": 31.44,
        |            "rentARoom": {
        |                "rentsReceived": 947.66
        |            }
        |        },
        |        "expenses": {
        |            "consolidatedExpense": 988.18
        |        }
        |    }
        |}
        |""".stripMargin
    )

    protected val requestData: Def1_AmendUkPropertyPeriodSummaryRequestData =
      Def1_AmendUkPropertyPeriodSummaryRequestData(
        Nino(validNino),
        TaxYear.fromMtd(taxYear),
        BusinessId(businessId),
        SubmissionId(submissionId),
        requestBodyWithoutConsolidatedExpenses)

    protected val requestDataConsolidatedExpenses: Def1_AmendUkPropertyPeriodSummaryRequestData =
      Def1_AmendUkPropertyPeriodSummaryRequestData(
        Nino(validNino),
        TaxYear.fromMtd(taxYear),
        BusinessId(businessId),
        SubmissionId(submissionId),
        requestBodyWithConsolidatedExpenses)

  }

}
