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

import api.controllers._
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.HistoricPropertyType
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v2.controllers.requestParsers.DeleteHistoricUkPropertyAnnualSubmissionRequestParser
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRawData
import v2.services.DeleteHistoricUkPropertyAnnualSubmissionService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                                    val lookupService: MtdIdLookupService,
                                                                    parser: DeleteHistoricUkPropertyAnnualSubmissionRequestParser,
                                                                    service: DeleteHistoricUkPropertyAnnualSubmissionService,
                                                                    auditService: AuditService,
                                                                    cc: ControllerComponents,
                                                                    idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  def handleFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext = {
      EndpointLogContext(
        controllerName = "DeleteHistoricUkPropertyAnnualSubmissionController",
        endpointName = "deleteHistoricFhlUkPropertyAnnualSubmission")
    }

    handleRequest(nino, taxYear, HistoricPropertyType.Fhl)
  }

  def handleNonFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(
        controllerName = "DeleteHistoricUkPropertyAnnualSubmissionController",
        endpointName = "deleteHistoricNonFhlUkPropertyAnnualSubmission")

    handleRequest(nino, taxYear, HistoricPropertyType.NonFhl)
  }

  def handleRequest(nino: String, taxYear: String, propertyType: HistoricPropertyType)(implicit
      endpointLogContext: EndpointLogContext): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)

      val requestHandler = RequestHandlerOld
        .withParser(parser)
        .withService(service.deleteHistoricUkPropertyAnnualSubmission)
        .withAuditing(auditHandler(nino, taxYear, propertyType, ctx.correlationId, request))

      requestHandler.handleRequest(rawData)

    }

  private def auditHandler(nino: String,
                           taxYear: String,
                           propertyType: HistoricPropertyType,
                           correlationId: String,
                           request: UserRequest[AnyContent]): AuditHandlerOld = {
    new AuditHandlerOld() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(
          implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {
        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              propertyType.toString,
              FlattenedGenericAuditDetail(
                Some("2.0"),
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                None,
                correlationId,
                AuditResponse(httpStatus, Left(err.auditErrors))
              )
            )
          case Right(_) =>
            auditSubmission(
              propertyType.toString,
              FlattenedGenericAuditDetail(
                Some("2.0"),
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                None,
                correlationId,
                AuditResponse(NO_CONTENT, Right(None))
              )
            )
        }
      }
    }
  }

  private def auditSubmission(propertyType: String, details: FlattenedGenericAuditDetail)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[AuditResult] = {

    val auditType: String       = s"DeleteHistoric${propertyType}PropertyBusinessAnnualSubmission"
    val transactionName: String = s"DeleteHistoric${propertyType}PropertyBusinessAnnualSubmission"
    val event                   = AuditEvent(auditType, transactionName, details)
    auditService.auditEvent(event)
  }

}
