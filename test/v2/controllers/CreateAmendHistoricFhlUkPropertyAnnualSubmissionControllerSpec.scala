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
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockAuditService, MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.TaxYear
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission._
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.{CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val nino          = "AA123456A"
  private val taxYear       = "2022-23"
  private val correlationId = "X-123"
  private val mtdId: String = "test-mtd-id"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockCreateAmendHistoricService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
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

  private val requestJson = Json.parse(
    """
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "balancingCharge": 200.00,
      |      "privateUseAdjustment": 200.00,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }   
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "otherCapitalAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 100.02,
      |      "propertyIncomeAllowance": 10.02
      |   }
      |}
      |""".stripMargin
  )

  private val annualAdjustments = HistoricFhlAnnualAdjustments(
    Some(BigDecimal("105.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("120.11")),
    periodOfGraceAdjustment = true,
    Some(BigDecimal("101.11")),
    nonResidentLandlord = false,
    Some(UkPropertyAdjustmentsRentARoom(true))
  )

  private val annualAllowances = HistoricFhlAnnualAllowances(
    Some(BigDecimal("100.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("425.11")),
    Some(BigDecimal("550.11"))
  )

  val body: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(
    Some(annualAdjustments),
    Some(annualAllowances)
  )

  private val rawData = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, requestJson)
  private val request = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), body)

  def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
      transactionName = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
      detail = FlattenedGenericAuditDetail(
        versionNumber = Some("2.0"),
        userDetails = UserDetails(mtdId, "Individual", None),
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = Some(requestJson),
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None)))))

        MockHateoasFactory
          .wrap(
            CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None),
            CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear)
          )
          .returns(HateoasWrapper(CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(None), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(requestJson))
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

            MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(requestJson))

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

            MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(requestJson))

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
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
