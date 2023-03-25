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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{MockAmendHistoricNonFhlUkPropertyPeriodSummaryService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.PeriodId
import api.models.UserDetails
import api.models.errors.{BadRequestError, ErrorWrapper, InternalError, MtdError, NinoFormatError, NotFoundError, PeriodIdFormatError, RuleBothExpensesSuppliedError, RuleIncorrectGovTestScenarioError, RuleIncorrectOrEmptyBodyError, ValueFormatError}
import api.models.ResponseWrapper
import api.models.audit.{AuditError, AuditResponse}
import api.models.domain.Nino
import api.models.hateoas.HateoasWrapper
import v2.models.audit.{AuditEvent, FlattenedGenericAuditDetail}
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.{AmendHistoricNonFhlUkPiePeriodSummaryRawData, AmendHistoricNonFhlUkPiePeriodSummaryRequest, AmendHistoricNonFhlUkPiePeriodSummaryRequestBody}
import v2.models.response.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricNonFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val nino          = "AA123456A"
  private val periodId      = "somePeriodId"
  private val correlationId = "X-123"
  private val mtdId: String = "test-mtd-id"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendHistoricNonFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendHistoricNonFhlUkPropertyPeriodSummaryRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right(mtdId)))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  // Doesn't matter what there are: parser is mocked
  private val requestBodyJson = JsObject.empty
  private val requestBody     = AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(None, None)

  private val rawData = AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, requestBodyJson)
  private val request = AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBody)

  def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] =
    AuditEvent(
      auditType = "AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary",
      transactionName = "AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary",
      detail = FlattenedGenericAuditDetail(
        versionNumber = Some("2.0"),
        userDetails = UserDetails(mtdId, "Individual", None),
        params = Map("nino" -> nino, "periodId" -> periodId),
        request = Some(requestBodyJson),
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendHistoricNonFhlUkPropertyPeriodSummaryRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData(nino, periodId))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe testHateoasLinksJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }
  }

  "return the error as per spec" when {
    "parser errors occur" should {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockAmendHistoricNonFhlUkPropertyPeriodSummaryRequestParser
            .parseRequest(rawData)
            .returns(Left(ErrorWrapper(correlationId, error, None)))

          val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))

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
        (withPath(RuleBothExpensesSuppliedError), BAD_REQUEST),
        (withPath(ValueFormatError), BAD_REQUEST),
        (withPath(RuleIncorrectOrEmptyBodyError), BAD_REQUEST),
        (PeriodIdFormatError, BAD_REQUEST)
      )

      input.foreach(args => (errorsFromParserTester _).tupled(args))
    }

    "service errors occur" should {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockAmendHistoricNonFhlUkPropertyPeriodSummaryRequestParser
            .parseRequest(rawData)
            .returns(Right(request))

          MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
            .amend(request)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
          MockedAuditService.verifyAuditEvent(event(auditResponse)).once
        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (PeriodIdFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (RuleBothExpensesSuppliedError, BAD_REQUEST),
        (InternalError, INTERNAL_SERVER_ERROR),
        (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }

}
