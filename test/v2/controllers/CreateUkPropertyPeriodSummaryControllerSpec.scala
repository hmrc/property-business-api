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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary._
import v2.controllers.validators.MockCreateUkPropertyPeriodSummaryValidatorFactory
import v2.models.request.createUkPropertyPeriodSummary.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.createUkPropertyPeriodSummary.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.services.MockCreateUkPropertyPeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockCreateUkPropertyPeriodSummaryService
    with MockCreateUkPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with MockAuditService {

  private val taxYear      = "2020-21"
  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "CreateUkPropertyPeriodSummaryController" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestDataConsolidatedExpense))

        MockCreateUkPropertyService
          .createUkProperty(requestDataConsolidatedExpense)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        override def callController(): Future[Result] =
          controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestBodyJsonConsolidatedExpense))

        runOkTestWithAudit(
          expectedStatus = CREATED,
          maybeAuditRequestBody = Some(requestBodyJsonConsolidatedExpense),
          maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas),
          maybeAuditResponseBody = Some(responseBodyJsonWithHateoas)
        )
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateUkPropertyService
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = CREATED,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas),
          maybeAuditResponseBody = Some(responseBodyJsonWithHateoas)
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

        MockCreateUkPropertyService
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
      service = mockCreateUkPropertyService,
      auditService = mockAuditService,
      validatorFactory = mockCreateUkPropertyPeriodSummaryValidatorFactory,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateUKPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "create-uk-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "businessId" -> businessId),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBody: CreateUkPropertyPeriodSummaryRequestBody =
      CreateUkPropertyPeriodSummaryRequestBody(
        "2020-01-01",
        "2020-01-31",
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

    private val requestBodyWithConsolidatedExpense: CreateUkPropertyPeriodSummaryRequestBody =
      CreateUkPropertyPeriodSummaryRequestBody(
        "2020-01-01",
        "2020-01-31",
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
      CreateUkPropertyPeriodSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), BusinessId(businessId), requestBody)

    protected val requestDataConsolidatedExpense: CreateUkPropertyPeriodSummaryRequestData =
      CreateUkPropertyPeriodSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), BusinessId(businessId), requestBodyWithConsolidatedExpense)

    protected val responseBodyJson: JsValue = Json.parse(
      s"""
         |{
         |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
         |}
    """.stripMargin
    )

    protected val responseData: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
      submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    )

    protected val hateoasData: CreateUkPropertyPeriodSummaryHateoasData =
      CreateUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson

  }

}
