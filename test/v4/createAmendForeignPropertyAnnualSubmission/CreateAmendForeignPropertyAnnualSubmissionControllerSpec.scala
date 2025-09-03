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

package v4.createAmendForeignPropertyAnnualSubmission

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_Fixtures
import v4.createAmendForeignPropertyAnnualSubmission.model.request.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendForeignPropertyAnnualSubmissionService
    with MockCreateAmendForeignPropertyAnnualSubmissionValidatorFactory
    with MockAuditService
    with MockIdGenerator
    with Def1_Fixtures {

  private val businessId                              = "XAIS12345678910"
  private val taxYear                                 = "2019-20"
  def fakeRequestWithBody[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "CreateAmendForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendForeignPropertyAnnualSubmissionService
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
    }

    "service errors occur" should {
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendForeignPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTest(NinoFormatError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new CreateAmendForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendForeignPropertyAnnualSubmissionValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakeRequestWithBody(requestJson))

    protected val requestJson: JsValue = createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendForeignPropertyAnnualSubmission",
        transactionName = "create-amend-foreign-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "4.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBody: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

    protected val requestData: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData(
        Nino(validNino),
        BusinessId(businessId),
        TaxYear.fromMtd(taxYear),
        requestBody
      )

  }

}
