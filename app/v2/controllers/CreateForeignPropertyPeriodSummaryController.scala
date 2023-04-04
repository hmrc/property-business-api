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
import api.models.errors.ErrorWrapper
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v2.controllers.requestParsers.CreateForeignPropertyPeriodSummaryRequestParser
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRawData
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryHateoasData
import v2.services.CreateForeignPropertyPeriodSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                              val lookupService: MtdIdLookupService,
                                                              parser: CreateForeignPropertyPeriodSummaryRequestParser,
                                                              service: CreateForeignPropertyPeriodSummaryService,
                                                              auditService: AuditService,
                                                              hateoasFactory: HateoasFactory,
                                                              cc: ControllerComponents,
                                                              idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateForeignPropertyPeriodSummaryController",
      endpointName = "Create a Foreign Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, body = request.body)

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.createForeignProperty)
          .withAuditing(auditHandler(rawData, request))
          .withHateoasResultFrom(hateoasFactory)(
            (_, response) => CreateForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, response.submissionId),
            CREATED)

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(rawData: CreateForeignPropertyPeriodSummaryRawData, request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(
          implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {
        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              GenericAuditDetail(
                request.userDetails,
                rawData,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))
              )
            )
          case Right(_) =>
            auditSubmission(
              GenericAuditDetail(
                request.userDetails,
                rawData,
                ctx.correlationId,
                AuditResponse(httpStatus = OK, response = Right(None))
              )
            )
        }
      }
    }
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("CreateForeignPropertyIncomeAndExpensesPeriodSummary", "create-foreign-property-income-and-expenses-period-summary", details)
    auditService.auditEvent(event)
  }

}
