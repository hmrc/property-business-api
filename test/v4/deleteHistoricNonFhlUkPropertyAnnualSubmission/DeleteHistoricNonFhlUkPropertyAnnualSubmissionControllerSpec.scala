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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockDeleteHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockDeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val taxYear = TaxYear.fromMtd("2021-22")

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

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val controller = new DeleteHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected val requestData: DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), taxYear)

    protected def callController(): Future[Result] = {
      val handler = controller.handleRequest(nino, taxYear.asMtd)
      handler(fakeDeleteRequest)
    }

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteHistoricNonFhlPropertyBusinessAnnualSubmission",
        transactionName = "delete-uk-property-historic-NonFhl-annual-submission",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails("some-mtdId", "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear.asMtd),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
