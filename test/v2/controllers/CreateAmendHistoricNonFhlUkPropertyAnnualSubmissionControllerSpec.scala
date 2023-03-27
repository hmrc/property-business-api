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
import api.models.hateoas.Method.GET
import api.models.domain.Nino
import api.models.errors._
import api.models.audit.{AuditError, AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.RequestResponseModelFixtures
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockAuditService, MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.TaxYear
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest}
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAuditService
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with RequestResponseModelFixtures {

  private val nino          = "AA123456A"
  private val taxYear       = "2022-23"
  private val correlationId = "X-123"
  val mtdId: String         = "test-mtd-id"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockCreateAmendHistoricService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLink = Link(href = s"/individuals/business/property/annual/$nino/annual/$taxYear", method = GET, rel = "self")

  val hateoasResponse: JsValue = Json.parse(s"""
       |{
       |   "links": [
       |      {
       |         "href": "/individuals/business/property/annual/$nino/annual/$taxYear",
       |         "method": "GET",
       |         "rel": "self"
       |      }
       |   ]
       |}
    """.stripMargin)

  private val rawData = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, validMtdJson)
  private val request = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAndAmendHistoricNonFhlPropertyBusinessAnnualSubmission",
      transactionName = "CreateAndAmendHistoricNonFhlPropertyBusinessAnnualSubmission",
      detail = FlattenedGenericAuditDetail(
        versionNumber = Some("2.0"),
        userDetails = UserDetails(mtdId, "Individual", None),
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = Some(validMtdJson),
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))))

        MockHateoasFactory
          .wrap(
            CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None),
            CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))
          .returns(HateoasWrapper(CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))

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
          (TaxYearFormatError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (
            ValueFormatError.copy(
              paths = Some(
                List(
                  "/annualAdjustments/lossBroughtForward",
                  "/annualAdjustments/balancingCharge",
                  "annualAllowances/annualInvestmentAllowance"
                ))
            ),
            BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
