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
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.mocks.MockIdGenerator
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{Json, JsValue}
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

  private val businessId    = "XAIS12345678910"
  private val taxYear       = "2019-20"

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendForeignPropertyAnnualSubmissionController(
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

    protected val testHateoasLink: Link = Link(href = s"/individuals/business/property/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

    protected val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links": [
         |      {
         |         "href": "/individuals/business/property/$nino/$businessId/annual/$taxYear",
         |         "method": "GET",
         |         "rel": "self"
         |      }
         |   ]
         |}
      """.stripMargin
    )

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendForeignPropertyAnnualSubmission",
        transactionName = "create-amend-foreign-property-annual-submission",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Json.obj("nino" -> nino, "businessId" -> businessId, "taxYear" -> "2019-20", "request" -> requestJson),
          correlationId = correlationId,
          response = auditResponse
        )
      )

    val body: CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

    protected val rawData: CreateAmendForeignPropertyAnnualSubmissionRawData =
      CreateAmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestJson)

    protected val request: CreateAmendForeignPropertyAnnualSubmissionRequest =
      CreateAmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), body)
  }

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {
        MockAmendForeignPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockAmendForeignPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(hateoasResponse))
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
          .returns(Right(request))

        MockAmendForeignPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

        runErrorTest(InternalError)
      }
    }
  }

}
