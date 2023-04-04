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
import api.hateoas.HateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v2.controllers.requestParsers.CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData
import v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData
import v2.services.CreateAmendHistoricFhlUkPropertyAnnualSubmissionService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                                            val lookupService: MtdIdLookupService,
                                                                            parser: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser,
                                                                            service: CreateAmendHistoricFhlUkPropertyAnnualSubmissionService,
                                                                            auditService: AuditService,
                                                                            hateoasFactory: HateoasFactory,
                                                                            cc: ControllerComponents,
                                                                            idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendHistoricFhlUkPropertyAnnualSubmissionController",
      endpointName = "CreateAmendHistoricFhlUkPropertyAnnualSubmission")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.amend)
          .withAuditing(auditHandler(nino, taxYear, ctx.correlationId, request))
          .withHateoasResult(hateoasFactory)(CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(nino: String, taxYear: String, correlationId: String, request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(
          implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {
        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some("2.0"),
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                Some(request.body),
                correlationId,
                AuditResponse(httpStatus, Left(err.auditErrors))
              )
            )
          case Right(_) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some("2.0"),
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                Some(request.body),
                correlationId,
                AuditResponse(OK, Right(None))
              )
            )
        }
      }
    }
  }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission", "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission", details)
    auditService.auditEvent(event)
  }

}
