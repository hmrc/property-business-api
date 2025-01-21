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

package v5.createUkPropertyPeriodSummary

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v5.createUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty._
import v5.createUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty._
import v5.createUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom._
import v5.createUkPropertyPeriodSummary.model.request._
import v5.createUkPropertyPeriodSummary.model.response.CreateUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateUkPropertyPeriodSummaryService
    with MockCreateUkPropertyPeriodSummaryValidatorFactory
    with MockAuditService {

  private val taxYear      = "2020-21"
  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "CreateUkPropertyPeriodSummaryController" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestDataConsolidatedExpense))

        MockedCreateUkPropertyPeriodSummaryService
          .createUkProperty(requestDataConsolidatedExpense)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        override def callController(): Future[Result] =
          controller.handleRequest(validNino, businessId, taxYear)(fakePostRequest(requestBodyJsonConsolidatedExpense))

        runOkTestWithAudit(
          expectedStatus = CREATED,
          maybeAuditRequestBody = Some(requestBodyJsonConsolidatedExpense),
          maybeExpectedResponseBody = Some(responseBodyJson),
          maybeAuditResponseBody = Some(responseBodyJson)
        )
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateUkPropertyPeriodSummaryService
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTestWithAudit(
          expectedStatus = CREATED,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseBodyJson),
          maybeAuditResponseBody = Some(responseBodyJson)
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

        MockedCreateUkPropertyPeriodSummaryService
          .createUkProperty(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new CreateUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateUkPropertyPeriodSummaryService,
      auditService = mockAuditService,
      validatorFactory = mockCreateUkPropertyPeriodSummaryValidatorFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakePostRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateUKPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "create-uk-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "9.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBody: Def1_CreateUkPropertyPeriodSummaryRequestBody =
      Def1_CreateUkPropertyPeriodSummaryRequestBody(
        "2020-01-01",
        "2020-01-31",
        Some(
          Def1_Create_UkFhlProperty(
            Some(
              Def1_Create_UkFhlPropertyIncome(
                Some(5000.99),
                Some(3123.21),
                Some(Def1_Create_UkPropertyIncomeRentARoom(
                  Some(532.12)
                ))
              )),
            Some(Def1_Create_UkFhlPropertyExpenses(
              Some(3123.21),
              Some(928.42),
              Some(842.99),
              Some(8831.12),
              Some(484.12),
              Some(99282),
              Some(999.99),
              Some(974.47),
              Some(Def1_Create_UkPropertyExpensesRentARoom(
                Some(8842.43)
              ))
            ))
          )),
        Some(
          Def1_Create_UkNonFhlProperty(
            Some(
              Def1_Create_UkNonFhlPropertyIncome(
                Some(41.12),
                Some(84.31),
                Some(9884.93),
                Some(842.99),
                Some(31.44),
                Some(Def1_Create_UkPropertyIncomeRentARoom(
                  Some(947.66)
                ))
              )),
            Some(
              Def1_Create_UkNonFhlPropertyExpenses(
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(988.18)
              ))
          ))
      )

    private val requestBodyWithConsolidatedExpense: Def1_CreateUkPropertyPeriodSummaryRequestBody =
      Def1_CreateUkPropertyPeriodSummaryRequestBody(
        "2020-01-01",
        "2020-01-31",
        Some(
          Def1_Create_UkFhlProperty(
            Some(
              Def1_Create_UkFhlPropertyIncome(
                Some(5000.99),
                Some(3123.21),
                Some(Def1_Create_UkPropertyIncomeRentARoom(
                  Some(532.12)
                ))
              )),
            Some(
              Def1_Create_UkFhlPropertyExpenses(
                None,
                None,
                None,
                None,
                None,
                None,
                Some(988.18),
                None,
                None
              ))
          )),
        Some(
          Def1_Create_UkNonFhlProperty(
            Some(
              Def1_Create_UkNonFhlPropertyIncome(
                Some(41.12),
                Some(84.31),
                Some(9884.93),
                Some(842.99),
                Some(31.44),
                Some(Def1_Create_UkPropertyIncomeRentARoom(
                  Some(947.66)
                ))
              )),
            Some(
              Def1_Create_UkNonFhlPropertyExpenses(
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(988.18)
              ))
          ))
      )

    protected val requestBodyJson: JsValue = Json.parse(
      """{
        |    "fromDate": "2020-01-01",
        |    "toDate": "2020-01-31",
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
        |    "fromDate": "2020-01-01",
        |    "toDate": "2020-01-31",
        |    "ukFhlProperty":{
        |        "income": {
        |            "periodAmount": 5000.99,
        |            "taxDeducted": 3123.21,
        |            "rentARoom": {
        |                "rentsReceived": 532.12
        |            }
        |        },
        |        "expenses": {
        |            "consolidatedExpenses": 988.18
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
        |            "consolidatedExpenses": 988.18
        |        }
        |    }
        |}
        |""".stripMargin
    )

    protected val requestData: CreateUkPropertyPeriodSummaryRequestData =
      Def1_CreateUkPropertyPeriodSummaryRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear), requestBody)

    protected val requestDataConsolidatedExpense: Def1_CreateUkPropertyPeriodSummaryRequestData =
      Def1_CreateUkPropertyPeriodSummaryRequestData(
        Nino(validNino),
        BusinessId(businessId),
        TaxYear.fromMtd(taxYear),
        requestBodyWithConsolidatedExpense)

    protected val responseBodyJson: JsValue = Json.parse(
      s"""
         |{
         |  "submissionId": "$submissionId"
         |}
    """.stripMargin
    )

    protected val responseData: CreateUkPropertyPeriodSummaryResponse =
      CreateUkPropertyPeriodSummaryResponse(submissionId = submissionId)

  }

}
