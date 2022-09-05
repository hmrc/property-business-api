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
import play.api.mvc.{ Action, AnyContent, Result }
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.requestParsers.MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{ MockAuditService, MockDeleteHistoricUkPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService }
import v2.models.domain.{ HistoricPropertyType, Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.{
  DeleteHistoricUkPropertyAnnualSubmissionRawData,
  DeleteHistoricUkPropertyAnnualSubmissionRequest
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteHistoricUkPropertyAnnualSubmissionService
    with MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2021-22"
  private val correlationId = "X-123"

  val controller = new DeleteHistoricUkPropertyAnnualSubmissionController(
    authService = mockEnrolmentsAuthService,
    lookupService = mockMtdIdLookupService,
    parser = mockDeleteHistoricUkPropertyAnnualSubmissionRequestParser,
    service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
    auditService = mockAuditService,
    cc = cc,
    idGenerator = mockIdGenerator
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private def rawData(propertyType: HistoricPropertyType) = DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)
  private def requestData(propertyType: HistoricPropertyType) =
    DeleteHistoricUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), propertyType)

  "handleRequest" should {
    "return No Content" when {
      def success(handler: Action[AnyContent], propertyType: HistoricPropertyType): Unit =
        "the request is valid and processed successfully" in new Test {
          MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
            .parse(rawData(propertyType))
            .returns(Right(requestData(propertyType)))

          MockDeleteHistoricUkPropertyAnnualSubmissionService
            .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          val result: Future[Result] = handler(fakeRequest)
          status(result) shouldBe NO_CONTENT
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }

      "FHL" when success(controller.handleFhlRequest(nino, taxYear), HistoricPropertyType.Fhl)
      "Non-FHL" when success(controller.handleNonFhlRequest(nino, taxYear), HistoricPropertyType.NonFhl)
    }

    "return the error as per spec" when {

      def parseErrors(handler: Action[AnyContent], propertyType: HistoricPropertyType): Unit =
        "parser errors occur" should {
          def parseError(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser" in new Test {

              MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
                .parse(rawData(propertyType))
                .returns(Left(ErrorWrapper(correlationId, error, None)))

              val result: Future[Result] = handler(fakeRequest)

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

          input.foreach(args => (parseError _).tupled(args))
        }

      "FHL" when parseErrors(controller.handleFhlRequest(nino, taxYear), propertyType = HistoricPropertyType.Fhl)
      "Non-FHL" when parseErrors(controller.handleNonFhlRequest(nino, taxYear), propertyType = HistoricPropertyType.NonFhl)

      def serviceErrors(handler: Action[AnyContent], propertyType: HistoricPropertyType): Unit =
        "service errors occur" should {
          def serviceError(mtdError: MtdError, expectedStatus: Int): Unit = {
            s"a $mtdError error is returned from the service" in new Test {

              MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
                .parse(rawData(propertyType))
                .returns(Right(requestData(propertyType)))

              MockDeleteHistoricUkPropertyAnnualSubmissionService
                .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
                .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

              val result: Future[Result] = handler(fakeRequest)

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(mtdError)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }

          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (TaxYearFormatError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
            (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
            (InternalError, INTERNAL_SERVER_ERROR)
          )

          input.foreach(args => (serviceError _).tupled(args))
        }

      "FHL" when serviceErrors(controller.handleFhlRequest(nino, taxYear), HistoricPropertyType.Fhl)
      "Non-FHL" when serviceErrors(controller.handleNonFhlRequest(nino, taxYear), HistoricPropertyType.NonFhl)

    }
  }
}
