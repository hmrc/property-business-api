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
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import utils.MockIdGenerator
import v3.controllers.validators.MockCreateAmendForeignPropertyAnnualSubmissionValidatorFactory
import v3.models.request.createAmendForeignPropertyAnnualSubmission._
import v3.models.response.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionHateoasData
import v3.services.MockCreateAmendForeignPropertyAnnualSubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendForeignPropertyAnnualSubmissionService
    with MockCreateAmendForeignPropertyAnnualSubmissionValidatorFactory
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator
    with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2019-20"

  "CreateAmendForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

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
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

    protected val requestJson: JsValue = createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendForeignPropertyAnnualSubmission",
        transactionName = "create-amend-foreign-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "3.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBody: CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

    protected val requestData: CreateAmendForeignPropertyAnnualSubmissionRequestData =
      CreateAmendForeignPropertyAnnualSubmissionRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), requestBody)

    protected val hateoasData: CreateAmendForeignPropertyAnnualSubmissionHateoasData =
      CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear)

  }

}