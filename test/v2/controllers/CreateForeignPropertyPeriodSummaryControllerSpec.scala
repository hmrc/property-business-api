/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockAuditService, MockCreateForeignPropertyPeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateForeignPropertyPeriodSummaryService
    with MockCreateForeignPropertyPeriodSummaryRequestParser
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2020-21"
  private val businessId    = "XAIS12345678910"
  private val submissionId  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateForeignPropertyService,
      parser = mockCreateForeignPropertyRequestParser,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val requestBody =
    CreateForeignPropertyPeriodSummaryRequestBody("2020-01-01", "2020-01-31", None, None)

  private val requestBodyJson = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31"
      |}
      |""".stripMargin
  )

  private val requestData =
    CreateForeignPropertyPeriodSummaryRequest(nino = Nino(nino), businessId = businessId, taxYear = taxYear, body = requestBody)
  private val rawData = CreateForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, body = requestBodyJson)

  private val testHateoasLinks =
    Seq(Link(href = "/some/link", method = GET, rel = "someRel"))

  private val hateoasResponse = Json.parse(
    s"""
       |{
       |  "submissionId": "$submissionId",
       |  "links": [
       |    {
       |      "href":"/some/link",
       |      "method":"GET",
       |      "rel":"someRel"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  private val response = CreateForeignPropertyPeriodSummaryResponse(submissionId)

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateForeignPropertyIncomeAndExpensesPeriodSummary",
      transactionName = "create-foreign-property-income-and-expenses-period-summary",
      detail = GenericAuditDetail(
        versionNumber = "2.0",
        userType = "Individual",
        agentReferenceNumber = None,
        params = Json.obj("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "request" -> requestBodyJson),
        correlationId = correlationId,
        response = auditResponse
      )
    )

  "create" should {
    "return a successful response" when {
      "the request received is valid" in new Test {
        MockCreateForeignPropertyRequestParser
          .requestFor(CreateForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, body = requestBodyJson))
          .returns(Right(requestData))

        MockCreateForeignPropertyService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response,
                CreateForeignPropertyPeriodSummaryHateoasData(nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId))
          .returns(HateoasWrapper(response, testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestBodyJson))

        contentAsJson(result) shouldBe hateoasResponse
        status(result) shouldBe CREATED
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(CREATED, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {
            MockCreateForeignPropertyRequestParser
              .requestFor(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] =
              controller.handleRequest(nino = nino, businessId = businessId, taxYear = taxYear)(fakeRequestWithBody(requestBodyJson))

            contentAsJson(result) shouldBe Json.toJson(error)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val paths = Some(Seq("somePath"))
        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (ValueFormatError.copy(paths = paths), BAD_REQUEST),
          (RuleBothExpensesSuppliedError.copy(paths = paths), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError.copy(paths = paths), BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (CountryCodeFormatError.copy(paths = paths), BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = paths), BAD_REQUEST),
          (RuleDuplicateCountryCodeError.copy(paths = paths), BAD_REQUEST),
          (BadRequestError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateForeignPropertyRequestParser
              .requestFor(rawData)
              .returns(Right(requestData))

            MockCreateForeignPropertyService
              .createForeignProperty(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] =
              controller.handleRequest(nino = nino, businessId = businessId, taxYear = taxYear)(fakeRequestWithBody(requestBodyJson))

            contentAsJson(result) shouldBe Json.toJson(mtdError)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (RuleDuplicateSubmissionError, BAD_REQUEST),
          (RuleMisalignedPeriodError, BAD_REQUEST),
          (RuleOverlappingPeriodError, BAD_REQUEST),
          (RuleNotContiguousPeriodError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (RuleDuplicateCountryCodeError, BAD_REQUEST),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
