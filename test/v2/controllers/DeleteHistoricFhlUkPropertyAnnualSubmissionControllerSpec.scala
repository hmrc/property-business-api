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
import v2.mocks.requestParsers.MockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{
  MockAuditService,
  MockDeleteHistoricFhlUkPropertyAnnualSubmissionService,
  MockEnrolmentsAuthService,
  MockMtdIdLookupService
}
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.deleteHistoricFhlUkPropertyAnnualSubmission.{
  DeleteHistoricFhlUkPropertyAnnualSubmissionRawData,
  DeleteHistoricFhlUkPropertyAnnualSubmissionRequest
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionService
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2021-22"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockDeleteHistoricFhlUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = DeleteHistoricFhlUkPropertyAnnualSubmissionRawData(nino, taxYear)
  private val requestData = DeleteHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  "handleRequest" should {
    "return No Content" when {
      "the request received is valid" in new Test {

        MockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteHistoricFhlUkPropertyAnnualSubmissionService
          .deleteHistoricFhlUkPropertyAnnualSubmission(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteHistoricFhlUkPropertyAnnualSubmissionRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteHistoricFhlUkPropertyAnnualSubmissionService
              .deleteHistoricFhlUkPropertyAnnualSubmission(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
