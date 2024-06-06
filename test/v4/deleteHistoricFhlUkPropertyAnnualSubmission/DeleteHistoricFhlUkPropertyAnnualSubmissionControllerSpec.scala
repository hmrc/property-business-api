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

package v4.deleteHistoricFhlUkPropertyAnnualSubmission

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.HistoricPropertyType.{Fhl, NonFhl}
import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v4.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionService
    with MockDeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockAuditService {

  private val taxYear               = TaxYear.fromMtd("2021-22")

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

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val propertyTypeValue: HistoricPropertyType

    private val controller = new DeleteHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory,
      service = mockDeleteHistoricUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = {
      val handler = propertyTypeValue match {
        case Fhl => controller.handleFhlRequest(nino, taxYear.asMtd)
        case _   => controller.handleNonFhlRequest(nino, taxYear.asMtd)
      }
      handler(fakeDeleteRequest)
    }

    protected def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] = {
      val fhlType: String = propertyTypeValue match {
        case HistoricPropertyType.Fhl => "Fhl"
        case _                        => "NonFhl"
      }

      AuditEvent(
        auditType = s"DeleteHistoric${fhlType}PropertyBusinessAnnualSubmission",
        transactionName = s"delete-uk-property-historic-$fhlType-annual-submission",
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

    protected def requestData(propertyType: HistoricPropertyType): Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), taxYear, propertyType)

  }

}
