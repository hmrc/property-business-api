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
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.HistoricPropertyType.{Fhl, NonFhl}
import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.requestParsers.MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockDeleteHistoricUkPropertyAnnualSubmissionService
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

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteHistoricUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteHistoricUkPropertyAnnualSubmissionRequestParser,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse, propertyType: HistoricPropertyType): AuditEvent[FlattenedGenericAuditDetail] = {
    val fhlType: String = propertyType match {
      case HistoricPropertyType.Fhl => "Fhl"
      case _                        => "NonFhl"
    }

    AuditEvent(
      auditType = s"DeleteHistoric${fhlType}PropertyBusinessAnnualSubmission",
      transactionName = s"DeleteHistoric${fhlType}PropertyBusinessAnnualSubmission",
      detail = FlattenedGenericAuditDetail(
        versionNumber = Some("2.0"),
        userDetails = UserDetails("some-mtdId", "Individual", None),
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = None,
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )
  }

  private def rawData(propertyType: HistoricPropertyType) = DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)

  private def requestData(propertyType: HistoricPropertyType) =
    DeleteHistoricUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), propertyType)

  "handleRequest" should {
    "return No Content" when {
      def success(propertyType: HistoricPropertyType): Unit = {
        s"${propertyType.toString} " should {
          "the request is valid and processed successfully" in new Test {

            val handler = propertyType match {
              case Fhl => controller.handleFhlRequest(nino, taxYear)
              case _   => controller.handleNonFhlRequest(nino, taxYear)
            }

            MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
              .parse(rawData(propertyType))
              .returns(Right(requestData(propertyType)))

            MockDeleteHistoricUkPropertyAnnualSubmissionService
              .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
              .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

            val result: Future[Result] = handler(fakeRequest)
            status(result) shouldBe NO_CONTENT
            header("X-CorrelationId", result) shouldBe Some(correlationId)
            val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
            MockedAuditService.verifyAuditEvent(event(auditResponse, propertyType)).once()
          }
        }

      }
      Seq(Fhl, NonFhl).foreach(c => success(c))
    }

    "return the error as per spec" when {

      def parseErrors(propertyType: HistoricPropertyType): Unit =
        "parser errors occur" should {
          def parseError(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser for  ${propertyType.toString}" in new Test {

              val handler = propertyType match {
                case Fhl => controller.handleFhlRequest(nino, taxYear)
                case _   => controller.handleNonFhlRequest(nino, taxYear)
              }
              MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
                .parse(rawData(propertyType))
                .returns(Left(ErrorWrapper(correlationId, error, None)))

              val result: Future[Result] = handler(fakeRequest)

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(error)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
              val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
              MockedAuditService.verifyAuditEvent(event(auditResponse, propertyType)).once
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
      Seq(Fhl, NonFhl).foreach(c => parseErrors(c))

      def serviceErrors(propertyType: HistoricPropertyType): Unit =
        "service errors occur" should {
          def serviceError(mtdError: MtdError, expectedStatus: Int): Unit = {
            s"a $mtdError error is returned from the service for ${propertyType.toString}" in new Test {

              val handler = propertyType match {
                case Fhl => controller.handleFhlRequest(nino, taxYear)
                case _   => controller.handleNonFhlRequest(nino, taxYear)
              }
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
              val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
              MockedAuditService.verifyAuditEvent(event(auditResponse, propertyType)).once
            }
          }

          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (TaxYearFormatError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
            (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
            (InternalError, INTERNAL_SERVER_ERROR),
            (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
          )

          input.foreach(args => (serviceError _).tupled(args))
        }

      Seq(Fhl, NonFhl).foreach(c => serviceErrors(c))
    }
  }

}
