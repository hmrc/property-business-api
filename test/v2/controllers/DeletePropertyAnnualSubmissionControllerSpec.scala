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
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockDeletePropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockDeletePropertyAnnualSubmissionService
import v2.models.request.deletePropertyAnnualSubmission._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeletePropertyAnnualSubmissionService
    with MockDeletePropertyAnnualSubmissionRequestParser
    with MockAuditService {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2023-24"

  private val rawData     = DeletePropertyAnnualSubmissionRawData(nino, businessId, taxYear)
  private val requestData = DeletePropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear))

  "DeletePropertyAnnualSubmissionController" should {
    "return No Content" when {
      "the request received is valid" in new Test {
        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeletePropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeletePropertyAnnualSubmissionService
          .deletePropertyAnnualSubmission(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeletePropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeletePropertyAnnualSubmissionRequestParser,
      service = mockDeletePropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePropertyAnnualSubmission",
        transactionName = "delete-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Json.obj("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
          correlationId = correlationId,
          response = auditResponse
        )
      )

  }

}
