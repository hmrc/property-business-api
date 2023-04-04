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
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.mocks.MockIdGenerator
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockDeletePropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockDeletePropertyAnnualSubmissionService
import v2.models.request.deletePropertyAnnualSubmission._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeletePropertyAnnualSubmissionService
    with MockDeletePropertyAnnualSubmissionRequestParser
    with MockAuditService
    with MockIdGenerator {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2023-24"

  "DeletePropertyAnnualSubmissionControllerSpec" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "the request received is valid" in new Test {

        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT, maybeExpectedResponseBody = None)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" in new Test {

        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, TaxYearFormatError, None)))

        runErrorTest(TaxYearFormatError)
      }

      "service errors returns an error" in new Test {

        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleIncorrectGovTestScenarioError))))

        runErrorTest(RuleIncorrectGovTestScenarioError)
      }
    }

  }

  trait Test extends ControllerTest {

    val controller = new DeletePropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeletePropertyAnnualSubmissionRequestParser,
      service = mockDeletePropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

    protected val rawData: DeletePropertyAnnualSubmissionRawData = DeletePropertyAnnualSubmissionRawData(nino, businessId, taxYear)

    protected val requestData: DeletePropertyAnnualSubmissionRequest =
      DeletePropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePropertyAnnualSubmission",
        transactionName = "delete-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Json.toJsObject(rawData),
          correlationId = correlationId,
          response = auditResponse
        )
      )

  }

}
