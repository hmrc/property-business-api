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

import api.controllers.ControllerBaseSpec
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import api.mocks.MockIdGenerator
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.requestParsers.MockCreateAmendForeignPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockCreateAmendForeignPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import v2.models.request.createAmendForeignPropertyAnnualSubmission._
import v2.models.response.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendForeignPropertyAnnualSubmissionService
    with MockCreateAmendForeignPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator
    with CreateAmendForeignPropertyAnnualSubmissionFixture {

  private val nino          = "AA123456A"
  private val businessId    = "XAIS12345678910"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

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

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLink = Link(href = s"/individuals/business/property/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

  val hateoasResponse: JsValue = Json.parse(
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

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
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

  private val requestJson = createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson

  val body: CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

  private val rawData = CreateAmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestJson)
  private val request = CreateAmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), body)

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

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (CountryCodeFormatError.copy(paths = Some(Seq("foreignNonFhlProperty/0/countryCode"))), BAD_REQUEST),
          (ValueFormatError.copy(
             paths = Some(Seq(
               "foreignFhlEea/adjustments/privateUseAdjustment",
               "foreignFhlEea/adjustments/balancingCharge",
               "foreignFhlEea/allowances/annualInvestmentAllowance",
               "foreignFhlEea/allowances/otherCapitalAllowance",
               "foreignFhlEea/allowances/propertyIncomeAllowance",
               "foreignFhlEea/allowances/electricChargePointAllowance",
               "foreignNonFhlProperty/adjustments/privateUseAdjustment",
               "foreignNonFhlProperty/adjustments/balancingCharge",
               "foreignNonFhlProperty/allowances/annualInvestmentAllowance",
               "foreignNonFhlProperty/allowances/costOfReplacingDomesticItems",
               "foreignNonFhlProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
               "foreignNonFhlProperty/allowances/propertyIncomeAllowance",
               "foreignNonFhlProperty/allowances/electricChargePointAllowance"
             ))),
           BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = Some(Seq("foreignNonFhlProperty/0/countryCode"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockAmendForeignPropertyAnnualSubmissionService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RulePropertyIncomeAllowanceError, BAD_REQUEST),
          (RuleDuplicateCountryCodeError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
