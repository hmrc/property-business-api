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

package v3.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v3.controllers.validators.MockAmendUkPropertyPeriodSummaryValidatorFactory
import v3.models.request.amendUkPropertyPeriodSummary._
import v3.models.request.amendUkPropertyPeriodSummary.ukFhlProperty._
import v3.models.request.amendUkPropertyPeriodSummary.ukNonFhlProperty._
import v3.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v3.models.response.amendUkPropertyPeriodSummary._
import v3.services.MockAmendUkPropertyPeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockAmendUkPropertyPeriodSummaryService
    with MockAmendUkPropertyPeriodSummaryValidatorFactory
    with MockAuditService
    with MockHateoasFactory {

  private val businessId   = "XAIS12345678910"
  private val taxYear      = "2020-21"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "AmendUkPropertyPeriodSummaryController" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestDataConsolidatedExpenses))

        MockAmendUkPropertyService
          .amend(requestDataConsolidatedExpenses)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), hateoasData)
          .returns(HateoasWrapper((), testHateoasLinks))

        override def callController(): Future[Result] =
          controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePutRequest(requestBodyJsonConsolidatedExpense))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJsonConsolidatedExpense),
          maybeExpectedResponseBody = Some(testHateoasLinksJson)
        )
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendUkPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(testHateoasLinksJson)
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
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] =
      controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendUKPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "amend-uk-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "3.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "submissionId" -> submissionId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBody: AmendUkPropertyPeriodSummaryRequestBody =
      AmendUkPropertyPeriodSummaryRequestBody(
        Some(
          UkFhlProperty(
            Some(
              UkFhlPropertyIncome(
                Some(5000.99),
                Some(3123.21),
                Some(UkPropertyIncomeRentARoom(
                  Some(532.12)
                ))
              )),
            Some(UkFhlPropertyExpenses(
              Some(3123.21),
              Some(928.42),
              Some(842.99),
              Some(8831.12),
              Some(484.12),
              Some(99282),
              Some(999.99),
              Some(974.47),
              Some(UkPropertyExpensesRentARoom(
                Some(8842.43)
              ))
            ))
          )),
        Some(
          UkNonFhlProperty(
            Some(
              UkNonFhlPropertyIncome(
                Some(41.12),
                Some(84.31),
                Some(9884.93),
                Some(842.99),
                Some(31.44),
                Some(UkPropertyIncomeRentARoom(
                  Some(947.66)
                ))
              )),
            Some(
              UkNonFhlPropertyExpenses(
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

    private val requestBodyWithConsolidatedExpenses: AmendUkPropertyPeriodSummaryRequestBody =
      AmendUkPropertyPeriodSummaryRequestBody(
        Some(
          UkFhlProperty(
            Some(
              UkFhlPropertyIncome(
                Some(5000.99),
                Some(3123.21),
                Some(UkPropertyIncomeRentARoom(
                  Some(532.12)
                ))
              )),
            Some(
              UkFhlPropertyExpenses(
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
          UkNonFhlProperty(
            Some(
              UkNonFhlPropertyIncome(
                Some(41.12),
                Some(84.31),
                Some(9884.93),
                Some(842.99),
                Some(31.44),
                Some(UkPropertyIncomeRentARoom(
                  Some(947.66)
                ))
              )),
            Some(
              UkNonFhlPropertyExpenses(
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

    protected val requestData: AmendUkPropertyPeriodSummaryRequestData =
      AmendUkPropertyPeriodSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), BusinessId(businessId), SubmissionId(submissionId), requestBody)

    protected val requestDataConsolidatedExpenses: AmendUkPropertyPeriodSummaryRequestData =
      AmendUkPropertyPeriodSummaryRequestData(
        Nino(nino),
        TaxYear.fromMtd(taxYear),
        BusinessId(businessId),
        SubmissionId(submissionId),
        requestBodyWithConsolidatedExpenses)

    protected val hateoasData: AmendUkPropertyPeriodSummaryHateoasData =
      AmendUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId)

  }

}
