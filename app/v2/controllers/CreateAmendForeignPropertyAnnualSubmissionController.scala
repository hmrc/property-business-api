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
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v2.controllers.requestParsers.CreateAmendForeignPropertyAnnualSubmissionRequestParser
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionRawData
import v2.models.response.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionHateoasData
import v2.models.response.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionResponse._
import v2.services.CreateAmendForeignPropertyAnnualSubmissionService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                                      val lookupService: MtdIdLookupService,
                                                                      parser: CreateAmendForeignPropertyAnnualSubmissionRequestParser,
                                                                      service: CreateAmendForeignPropertyAnnualSubmissionService,
                                                                      auditService: AuditService,
                                                                      hateoasFactory: HateoasFactory,
                                                                      cc: ControllerComponents,
                                                                      idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendForeignPropertyAnnualSubmissionController",
      endpointName = "CreateAmendForeignPropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateAmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.createAmendForeignPropertyAnnualSubmission)
        .withAuditing(
          auditHandler(rawData = rawData, correlationId = ctx.correlationId, nino = nino, businessId = businessId, taxYear = taxYear, request = request))
        .withHateoasResult(hateoasFactory)(CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear), OK)

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(rawData: CreateAmendForeignPropertyAnnualSubmissionRawData,
                           correlationId: String,
                           nino: String,
                           businessId: String,
                           taxYear: String,
                           request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(
          implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {
        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              details = GenericAuditDetail(
                userDetails = request.userDetails,
                params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
                correlationId = correlationId,
                response = AuditResponse(httpStatus, Left(err.auditErrors))
              )
            )

          case Right(_) =>
            auditSubmission(
              GenericAuditDetail(
                request.userDetails,
                Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
                ctx.correlationId,
                AuditResponse(httpStatus = OK, response = Right(None))
              )
            )
        }
      }
    }

  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendForeignPropertyAnnualSubmission", "create-amend-foreign-property-annual-submission", details)
    auditService.auditEvent(event)
  }

}
