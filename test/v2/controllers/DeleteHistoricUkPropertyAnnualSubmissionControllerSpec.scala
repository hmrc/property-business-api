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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.HistoricPropertyType.{Fhl, NonFhl}
import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
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
    with ControllerTestRunner
    with MockDeleteHistoricUkPropertyAnnualSubmissionService
    with MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
    with MockAuditService {

  private val taxYear = "2021-22"

  "DeleteHistoricUkPropertyAnnualSubmissionController" should {
    "return No Content" when {
      def success(propertyType: HistoricPropertyType): Unit = {
        s"${propertyType.toString} " should {
          "the request is valid and processed successfully" in new Test {
            val propertyTypeValue: HistoricPropertyType = propertyType

            MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
              .parse(rawData(propertyType))
              .returns(Right(requestData(propertyType)))

            MockDeleteHistoricUkPropertyAnnualSubmissionService
              .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
              .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

            runOkTestWithAudit(NO_CONTENT, None)
          }
        }
      }
      Seq(Fhl, NonFhl).foreach(c => success(c))
    }

    "return the error as per spec" when {

      def parseErrors(propertyType: HistoricPropertyType): Unit =
        s"the parser validation fails for ${propertyType.toString}" in new Test {
          val propertyTypeValue: HistoricPropertyType = propertyType

          MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
            .parse(rawData(propertyType))
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

          runErrorTestWithAudit(NinoFormatError)
        }
      Seq(Fhl, NonFhl).foreach(c => parseErrors(c))

      def serviceErrors(propertyType: HistoricPropertyType): Unit =
        s"service returns an error ${propertyType.toString}" in new Test {
          val propertyTypeValue: HistoricPropertyType = propertyType

          MockDeleteHistoricUkPropertyAnnualSubmissionRequestParser
            .parse(rawData(propertyType))
            .returns(Right(requestData(propertyType)))

          MockDeleteHistoricUkPropertyAnnualSubmissionService
            .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

          runErrorTestWithAudit(RuleTaxYearNotSupportedError)
        }
      Seq(Fhl, NonFhl).foreach(c => serviceErrors(c))
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    val propertyTypeValue: HistoricPropertyType

    val controller = new DeleteHistoricUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteHistoricUkPropertyAnnualSubmissionRequestParser,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = {
      val handler = propertyTypeValue match {
        case Fhl => controller.handleFhlRequest(nino, taxYear)
        case _   => controller.handleNonFhlRequest(nino, taxYear)
      }
      handler(fakeDeleteRequest)
    }

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] = {
      val fhlType: String = propertyTypeValue match {
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
          request = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )
    }

    protected def rawData(propertyType: HistoricPropertyType): DeleteHistoricUkPropertyAnnualSubmissionRawData =
      DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)

    protected def requestData(propertyType: HistoricPropertyType): DeleteHistoricUkPropertyAnnualSubmissionRequest =
      DeleteHistoricUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), propertyType)

  }

}
