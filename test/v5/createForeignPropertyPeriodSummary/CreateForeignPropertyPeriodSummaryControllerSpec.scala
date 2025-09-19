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

package v5.createForeignPropertyPeriodSummary

import common.models.errors.RuleMisalignedPeriodError
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v5.createForeignPropertyPeriodSummary.def1.model.Def1_CreateForeignPropertyPeriodSummaryFixtures
import v5.createForeignPropertyPeriodSummary.model.request.{
  CreateForeignPropertyPeriodSummaryRequestData,
  Def1_CreateForeignPropertyPeriodSummaryRequestData
}
import v5.createForeignPropertyPeriodSummary.model.response.CreateForeignPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateForeignPropertyPeriodSummaryService
    with MockCreateForeignPropertyPeriodSummaryValidatorFactory
    with MockAuditService
    with MockIdGenerator
    with Def1_CreateForeignPropertyPeriodSummaryFixtures {

  private val taxYear      = "2020-21"
  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "CreateForeignPropertyPeriodSummaryControllerSpec" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateForeignPropertyPeriodSummaryService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(mtdResponse))

      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(NinoFormatError)
        }

        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockedCreateForeignPropertyPeriodSummaryService
            .createForeignProperty(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

          runErrorTest(RuleMisalignedPeriodError)
        }
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateForeignPropertyPeriodSummaryController = new CreateForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateForeignPropertyPeriodSummaryService,
      validatorFactory = mockCreateForeignPropertyPeriodSummaryValidatorFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakePostRequest(requestBody))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateForeignPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "create-foreign-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "4.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear, "businessId" -> businessId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    val requestBody: JsObject = JsObject.empty

    protected val requestData: CreateForeignPropertyPeriodSummaryRequestData =
      Def1_CreateForeignPropertyPeriodSummaryRequestData(
        nino = Nino(validNino),
        businessId = BusinessId(businessId),
        taxYear = TaxYear.fromMtd(taxYear),
        body = regularExpensesRequestBody)

    protected val mtdResponse: JsObject = Json
      .parse(
        s"""
           |{
           |  "submissionId": "$submissionId"
           |}
      """.stripMargin
      )
      .as[JsObject]

    protected val response: CreateForeignPropertyPeriodSummaryResponse = CreateForeignPropertyPeriodSummaryResponse(submissionId)
  }

}
