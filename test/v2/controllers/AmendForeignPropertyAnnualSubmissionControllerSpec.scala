/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockAmendForeignPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockAmendForeignPropertyAnnualSubmissionService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, CreateAndAmendForeignPropertyAnnualAuditDetail}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.hateoas.Method.GET
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyAnnualSubmission._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignProperty._
import v2.models.response.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyAnnualSubmissionControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyAnnualSubmissionService
    with MockAmendForeignPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyAnnualSubmissionRequestParser,
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

  private val testHateoasLink = Link(href = s"/Individuals/business/property/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links": [
      |      {
      |         "href": "/Individuals/business/property/$nino/$businessId/annual/$taxYear",
      |         "method": "GET",
      |         "rel": "self"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  def event(auditResponse: AuditResponse): AuditEvent[CreateAndAmendForeignPropertyAnnualAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendForeignPropertyAnnualSummary",
      transactionName = "Create-Amend-Foreign-Property-Annual-Summary",
      detail = CreateAndAmendForeignPropertyAnnualAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        businessId,
        taxYear,
        requestJson,
        correlationId,
        response = auditResponse
      )
    )

  private val requestJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25,
      |        "periodOfGraceAdjustment":true
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    },
      |  "foreignProperty": [
      |    {
      |      "countryCode":"GER",
      |      "adjustments": {
      |        "privateUseAdjustment":100.25,
      |        "balancingCharge":100.25
      |      },
      |      "allowances": {
      |        "annualInvestmentAllowance":100.25,
      |        "costOfReplacingDomesticItems":100.25,
      |        "zeroEmissionsGoodsVehicleAllowance":100.25,
      |        "propertyAllowance":100.25,
      |        "otherCapitalAllowance":100.25,
      |        "structureAndBuildingAllowance":100.25,
      |        "electricChargePointAllowance":100.25
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val foreignFhlEea = ForeignFhlEea(
    Some(ForeignFhlEeaAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(true)
    )),
    Some(ForeignFhlEeaAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ))
  )

  private val foreignPropertyEntry = ForeignPropertyEntry(
    "FRA",
    Some(ForeignPropertyAdjustments(
      Some(5000.99),
      Some(5000.99)
    )),
    Some(ForeignPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ))
  )

  val body: AmendForeignPropertyAnnualSubmissionRequestBody = AmendForeignPropertyAnnualSubmissionRequestBody(
    Some(foreignFhlEea),
    Some(Seq(foreignPropertyEntry))
  )

  private val rawData = AmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestJson)
  private val request = AmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

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
          .wrap((), AmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
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

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestJson))

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
          (CountryCodeFormatError.copy(paths = Some(Seq(
            "foreignProperty/0/countryCode"))), BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "foreignFhlEea/adjustments/privateUseAdjustment",
            "foreignFhlEea/adjustments/balancingCharge",
            "foreignFhlEea/allowances/annualInvestmentAllowance",
            "foreignFhlEea/allowances/otherCapitalAllowance",
            "foreignFhlEea/allowances/propertyAllowance",
            "foreignFhlEea/allowances/electricChargePointAllowance",
            "foreignProperty/adjustments/privateUseAdjustment",
            "foreignProperty/adjustments/balancingCharge",
            "foreignProperty/allowances/annualInvestmentAllowance",
            "foreignProperty/allowances/costOfReplacingDomesticItems",
            "foreignProperty/allowances/zeroEmissionsGoodsVehicleAllowance",
            "foreignProperty/allowances/propertyAllowance",
            "foreignProperty/allowances/otherCapitalAllowance",
            "foreignProperty/allowances/structureAndBuildingAllowance",
            "foreignProperty/allowances/electricChargePointAllowance"
          ))), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = Some(Seq(
            "foreignProperty/0/countryCode"))), BAD_REQUEST)
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

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestJson))

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
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}