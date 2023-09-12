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
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetailOld}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockIdGenerator
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockCreateAmendForeignPropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockCreateAmendForeignPropertyAnnualSubmissionService
import v2.models.request.createAmendForeignPropertyAnnualSubmission._
import v2.models.response.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendForeignPropertyAnnualSubmissionService
    with MockCreateAmendForeignPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator
    with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2019-20"

  "CreateAmendForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAmendForeignPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendForeignPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), hateoasData)
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(testHateoasLinksJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAmendForeignPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError, None)))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }

    "service errors occur" should {
      "the service returns an error" in new Test {
        MockAmendForeignPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendForeignPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

        runErrorTest(InternalError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetailOld] {

    private val controller = new CreateAmendForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendForeignPropertyAnnualSubmissionRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

    protected val requestJson: JsValue = createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetailOld] =
      AuditEvent(
        auditType = "CreateAmendForeignPropertyAnnualSubmission",
        transactionName = "create-amend-foreign-property-annual-submission",
        detail = GenericAuditDetailOld(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Json.toJsObject(rawData),
          correlationId = correlationId,
          response = auditResponse
        )
      )

    private val requestBody: CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

    protected val rawData: CreateAmendForeignPropertyAnnualSubmissionRawData =
      CreateAmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestJson)

    protected val requestData: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      CreateAmendForeignPropertyAnnualSubmissionRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), requestBody)

    protected val hateoasData: CreateAmendForeignPropertyAnnualSubmissionHateoasData =
      CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear)

  }

}
