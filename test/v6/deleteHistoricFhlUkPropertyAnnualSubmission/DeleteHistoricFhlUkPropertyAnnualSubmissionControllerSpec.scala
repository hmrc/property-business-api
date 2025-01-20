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

package v6.deleteHistoricFhlUkPropertyAnnualSubmission

import common.models.domain.HistoricPropertyType
import common.models.domain.HistoricPropertyType.{Fhl, NonFhl}
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
import v6.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionService
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val taxYear = TaxYear.fromMtd("2021-22")

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  "DeleteHistoricFhlUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      def success(propertyType: HistoricPropertyType): Unit = {
        s"${propertyType.toString} " should {
          "the request is valid and processed successfully" in new Test {
            val propertyTypeValue: HistoricPropertyType = propertyType

            willUseValidator(returningSuccess(requestData(propertyType)))

            MockDeleteHistoricUkPropertyAnnualSubmissionService
              .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
              .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

            runOkTestWithAudit(NO_CONTENT, None)
          }
        }
      }
      List(Fhl, NonFhl).foreach(c => success(c))
    }

    "return the error as per spec" when {

      def parseErrors(propertyType: HistoricPropertyType): Unit =
        s"the parser validation fails for ${propertyType.toString}" in new Test {
          val propertyTypeValue: HistoricPropertyType = propertyType

          willUseValidator(returning(NinoFormatError))

          runErrorTestWithAudit(NinoFormatError)
        }
      List(Fhl, NonFhl).foreach(c => parseErrors(c))

      def serviceErrors(propertyType: HistoricPropertyType): Unit =
        s"service returns an error ${propertyType.toString}" in new Test {
          val propertyTypeValue: HistoricPropertyType = propertyType

          willUseValidator(returningSuccess(requestData(propertyType)))

          MockDeleteHistoricUkPropertyAnnualSubmissionService
            .deleteHistoricUkPropertyAnnualSubmission(requestData(propertyType))
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

          runErrorTestWithAudit(RuleTaxYearNotSupportedError)
        }
      List(Fhl, NonFhl).foreach(c => serviceErrors(c))
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val propertyTypeValue: HistoricPropertyType

    protected val controller = new DeleteHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = {
      val handler = propertyTypeValue match {
        case Fhl => controller.handleFhlRequest(validNino, taxYear.asMtd)
        case _   => controller.handleNonFhlRequest(validNino, taxYear.asMtd)
      }
      handler(fakeDeleteRequest)
    }

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] = {
      val fhlType: String = propertyTypeValue match {
        case HistoricPropertyType.Fhl => "Fhl"
        case _                        => "NonFhl"
      }

      AuditEvent(
        auditType = s"DeleteHistoric${fhlType}PropertyBusinessAnnualSubmission",
        transactionName = s"delete-uk-property-historic-$fhlType-annual-submission",
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

    protected def requestData(propertyType: HistoricPropertyType): Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(validNino), taxYear, propertyType)

  }

}
