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

package v5.createAmendUkPropertyCumulativeSummary

import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request._
import v5.createAmendUkPropertyCumulativeSummary.model.request.CreateAmendUkPropertyCumulativeSummaryRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkPropertyCumulativeSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockCreateAmendUkPropertyCumulativeSummaryService
    with MockCreateAmendUkPropertyCumulativeSummaryValidatorFactory
    with MockAuditService {

  private val taxYear    = "2020-21"
  private val businessId = "XAIS12345678910"

  "CreateAmendUkPropertyCumulativeSummaryController" should {
    "return a successful response from a valid request" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendUkPropertyCumulativeSummaryService
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        override def callController(): Future[Result] =
          controller.handleRequest(validNino, businessId, taxYear)(fakePostRequest(requestBodyJson))

        runOkTestWithAudit(
          expectedStatus = NO_CONTENT,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
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

        MockCreateAmendUkPropertyCumulativeSummaryService
          .createUkProperty(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new CreateAmendUkPropertyCumulativeSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateAmendUkPropertyCumulativeSummaryService,
      auditService = mockAuditService,
      validatorFactory = mockCreateAmendUkPropertyCumulativeSummaryValidatorFactory,
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
        auditType = "CreateAmendUkPropertyCumulativeSummary",
        transactionName = "create-amend-uk-property-cumulative-summary",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

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

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "fromDate": "2023-04-01",
        |  "toDate": "2024-04-01",
        |  "ukProperty": {
        |    "income": {
        |      "premiumsOfLeaseGrant": 42.12,
        |      "reversePremiums": 84.31,
        |      "periodAmount": 9884.93,
        |      "taxDeducted": 842.99,
        |      "otherIncome": 31.44,
        |      "rentARoom": {
        |        "rentsReceived": 947.66
        |      }
        |    },
        |    "expenses": {
        |      "premisesRunningCosts": 1500.50,
        |      "repairsAndMaintenance": 1200.75,
        |      "financialCosts": 2000.00,
        |      "professionalFees": 500.00,
        |      "costOfServices": 300.25,
        |      "other": 100.50,
        |      "residentialFinancialCost": 9000.10,
        |      "travelCosts": 400.00,
        |      "residentialFinancialCostsCarriedForward": 300.13,
        |      "rentARoom": {
        |        "amountClaimed": 860.88
        |      }
        |    }
        |  }
        |}
  """.stripMargin
    )

    protected val requestData: CreateAmendUkPropertyCumulativeSummaryRequestData =
      Def1_CreateAmendUkPropertyCumulativeSummaryRequestData(Nino(validNino), TaxYear.fromMtd(taxYear), BusinessId(businessId), requestBody)

  }

}
