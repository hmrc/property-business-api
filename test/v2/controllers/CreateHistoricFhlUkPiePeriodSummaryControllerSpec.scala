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
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{MockAuditService, MockCreateHistoricFhlUkPiePeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.PeriodId
import api.models.errors.{ErrorWrapper, FromDateFormatError, InternalError, MtdError, NinoFormatError, NotFoundError, RuleBothExpensesSuppliedError, RuleDuplicateSubmissionError, RuleIncorrectGovTestScenarioError, RuleIncorrectOrEmptyBodyError, RuleMisalignedPeriodError, RuleNotContiguousPeriodError, RuleOverlappingPeriodError, RuleTaxYearNotSupportedError, RuleToDateBeforeFromDateError, ServiceUnavailableError, ToDateFormatError, ValueFormatError}
import api.models.hateoas.Method.GET
import api.models.audit.{AuditError, AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.Nino
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryRawData, CreateHistoricFhlUkPiePeriodSummaryRequest, CreateHistoricFhlUkPiePeriodSummaryRequestBody}
import v2.models.response.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryHateoasData, CreateHistoricFhlUkPiePeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricFhlUkPiePeriodSummaryService
    with MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val nino: String          = "AA123456A"
  private val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val periodId              = "2021-01-01_2021-01-02"
  private val mtdId: String         = "test-mtd-id"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: CreateHistoricFhlUkPiePeriodSummaryController = new CreateHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateHistoricFhlUkPiePeriodSummaryRequestParser,
      service = mockCreateHistoricFhlUkPiePeriodSummaryService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody("2021-01-01", "2021-01-02", None, None)

    private val requestBodyJson: JsValue = Json.parse(
      """{
        |    "fromDate": "2021-01-01",
        |    "toDate": "2021-01-02"
        |}
        |""".stripMargin
    )

    private val requestData: CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), requestBody)
    private val rawData: CreateHistoricFhlUkPiePeriodSummaryRawData     = CreateHistoricFhlUkPiePeriodSummaryRawData(nino, requestBodyJson)

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)

    private val hateoasLinks: Seq[Link] = Seq(Link(href = "/the-link/", method = GET, rel = "the-rel"))

    private val hateoasResponse: JsValue = Json.parse(s"""
         |{
         |  "links":[{
         |    "href": "/the-link",
         |    "method": "GET",
         |    "rel":"the-rel"
         |  }]         
         |}
         |""".stripMargin)

    private val response: CreateHistoricFhlUkPiePeriodSummaryResponse = CreateHistoricFhlUkPiePeriodSummaryResponse(PeriodId(periodId))

    def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("2.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino),
          request = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    "Create" should {
      "return a successful response " when {
        "the request received is valid" in new Test {
          MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockCreateHistoricFhlUkPiePeriodSummaryService
            .createPeriodSummary(requestData)
            .returns(
              Future.successful(Right(ResponseWrapper(correlationId, response)))
            )

          MockHateoasFactory
            .wrap(response, CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino, PeriodId(periodId)))
            .returns(HateoasWrapper(response, hateoasLinks))

          val result: Future[Result] = controller.handleRequest(nino)(fakeRequestWithBody(requestBodyJson))

          contentAsJson(result) shouldBe hateoasResponse
          status(result) shouldBe CREATED
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(OK, None, None)
          MockedAuditService.verifyAuditEvent(event(auditResponse)).once
        }
      }

      "return the error as per spec" when {
        "parser errors occur" must {
          def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser" in new Test {
              MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
                .parseRequest(rawData)
                .returns(Left(ErrorWrapper(correlationId, error, None)))

              val result: Future[Result] =
                controller.handleRequest(nino = nino)(fakeRequestWithBody(requestBodyJson))

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
            (RuleBothExpensesSuppliedError.copy(paths = paths), BAD_REQUEST),
            (ValueFormatError.copy(paths = paths), BAD_REQUEST),
            (RuleIncorrectOrEmptyBodyError.copy(paths = paths), BAD_REQUEST),
            (ToDateFormatError, BAD_REQUEST),
            (FromDateFormatError, BAD_REQUEST),
            (RuleToDateBeforeFromDateError, BAD_REQUEST)
          )
          input.foreach(args => (errorsFromParserTester _).tupled(args))
        }

        "service errors occur" must {
          def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
            s"a $mtdError error is returned from the service" in new Test {

              MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
                .parseRequest(rawData)
                .returns(Right(requestData))

              MockCreateHistoricFhlUkPiePeriodSummaryService
                .createPeriodSummary(requestData)
                .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

              val result: Future[Result] =
                controller.handleRequest(nino = nino)(fakeRequestWithBody(requestBodyJson))

              contentAsJson(result) shouldBe Json.toJson(mtdError)
              status(result) shouldBe expectedStatus
              header("X-CorrelationId", result) shouldBe Some(correlationId)

              val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
              MockedAuditService.verifyAuditEvent(event(auditResponse)).once
            }
          }

          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
            (RuleDuplicateSubmissionError, BAD_REQUEST),
            (RuleMisalignedPeriodError, BAD_REQUEST),
            (RuleOverlappingPeriodError, BAD_REQUEST),
            (RuleNotContiguousPeriodError, BAD_REQUEST),
            (RuleToDateBeforeFromDateError, BAD_REQUEST),
            (RuleTaxYearNotSupportedError, BAD_REQUEST),
            (InternalError, INTERNAL_SERVER_ERROR),
            (ServiceUnavailableError, INTERNAL_SERVER_ERROR),
            (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
          )
          input.foreach(args => (serviceErrors _).tupled(args))
        }
      }
    }

  }

}
