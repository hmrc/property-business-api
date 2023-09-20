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
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v2.controllers.validators.MockDeletePropertyAnnualSubmissionValidatorFactory
import v2.models.request.deletePropertyAnnualSubmission._
import v2.services.MockDeletePropertyAnnualSubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeletePropertyAnnualSubmissionService
    with MockDeletePropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2023-24")

  "DeletePropertyAnnualSubmissionControllerSpec" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT, maybeExpectedResponseBody = None)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    private val controller = new DeletePropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeletePropertyAnnualSubmissionValidatorFactory,
      service = mockDeletePropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId.businessId, taxYear.asMtd)(fakeDeleteRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePropertyAnnualSubmission",
        transactionName = "delete-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "businessId" -> businessId.businessId, "taxYear" -> taxYear.asMtd),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    protected val requestData: DeletePropertyAnnualSubmissionRequestData =
      DeletePropertyAnnualSubmissionRequestData(Nino(nino), businessId, taxYear)

  }

}
