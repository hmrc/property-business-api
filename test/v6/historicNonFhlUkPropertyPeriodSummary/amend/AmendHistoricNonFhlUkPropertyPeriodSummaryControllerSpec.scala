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

package v6.historicNonFhlUkPropertyPeriodSummary.amend

import common.models.domain.PeriodId
import common.models.errors.RuleMisalignedPeriodError
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.historicNonFhlUkPropertyPeriodSummary.amend.model.request._
import v6.historicNonFhlUkPropertyPeriodSummary.create.MockAmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricNonFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId                           = "2017-04-06_2017-07-04"
  private val mtdId: String                      = "test-mtd-id"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "AmendHistoricNonFhlUkPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedAmendHistoricNonFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
        )
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedAmendHistoricNonFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTestWithAudit(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new AmendHistoricNonFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendHistoricNonFhlUkPeriodSummaryValidatorFactory,
      service = mockAmendHistoricNonFhlUkPropertyPeriodSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, periodId)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "amend-historic-non-fhl-property-income-expenses-period-summary",
        detail = GenericAuditDetail(
          userDetails = UserDetails(mtdId, "Individual", None),
          apiVersion = apiVersion.name,
          params = Map("nino" -> validNino, "periodId" -> periodId),
          requestBody = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    protected val requestBodyJson: JsValue = JsObject.empty

    protected val requestBody: Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(None, None)

    protected val requestData: AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData(Nino(validNino), PeriodId(periodId), requestBody)

  }

}
