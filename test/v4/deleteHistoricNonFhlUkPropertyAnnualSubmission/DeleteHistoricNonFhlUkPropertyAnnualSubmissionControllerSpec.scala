/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission

import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockDeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val taxYear = TaxYear.fromMtd("2021-22")

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  "DeleteHistoricUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      def success(): Unit = {
        "the request is valid and processed successfully" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockDeleteHistoricUkPropertyAnnualSubmissionService
            .deleteHistoricUkPropertyAnnualSubmission(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          runOkTestWithAudit(NO_CONTENT, None)
        }
      }
      success()
    }

    "return the error as per spec" when {

      def parseErrors(): Unit =
        s"the parser validation fails" in new Test {
          willUseValidator(returning(NinoFormatError))

          runErrorTestWithAudit(NinoFormatError)
        }

      parseErrors()

      def serviceErrors(): Unit =
        s"service returns an error" in new Test {

          willUseValidator(returningSuccess(requestData))

          MockDeleteHistoricUkPropertyAnnualSubmissionService
            .deleteHistoricUkPropertyAnnualSubmission(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

          runErrorTestWithAudit(RuleTaxYearNotSupportedError)
        }

      serviceErrors()
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new DeleteHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected val requestData: DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(validNino), taxYear)

    protected def callController(): Future[Result] = {
      val handler = controller.handleRequest(validNino, taxYear.asMtd)
      handler(fakeDeleteRequest)
    }

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteHistoricNonFhlPropertyBusinessAnnualSubmission",
        transactionName = "delete-uk-property-historic-NonFhl-annual-submission",
        detail = GenericAuditDetail(
          userDetails = UserDetails("some-mtdId", "Individual", None),
          apiVersion = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear.asMtd),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
