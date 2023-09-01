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
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetailOld}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v2.controllers.requestParsers.AmendForeignPropertyPeriodSummaryRequestParser
import v2.models.request.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryRawData
import v2.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryHateoasData
import v2.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryResponse.AmendForeignPropertyLinksFactory
import v2.services.AmendForeignPropertyPeriodSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendForeignPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             parser: AmendForeignPropertyPeriodSummaryRequestParser,
                                                             service: AmendForeignPropertyPeriodSummaryService,
                                                             auditService: AuditService,
                                                             hateoasFactory: HateoasFactory,
                                                             cc: ControllerComponents,
                                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendForeignPropertyPeriodSummaryController", endpointName = "amendForeignPropertyPeriodSummary")

  def handleRequest(nino: String, businessId: String, taxYear: String, submissionId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, request.body)

      val requestHandler =
        RequestHandlerOld
          .withParser(parser)
          .withService(service.amendForeignPropertyPeriodSummary)
          .withAuditing(auditHandler(rawData, ctx.correlationId, request))
          .withHateoasResult(hateoasFactory)(AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(rawData: AmendForeignPropertyPeriodSummaryRawData,
                           correlationId: String,
                           request: UserRequest[JsValue]): AuditHandlerOld = {
    new AuditHandlerOld() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]], versionNumber: String)(
          implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {
        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(GenericAuditDetailOld(request.userDetails, rawData, correlationId, AuditResponse(httpStatus, Left(err.auditErrors))))
          case Right(_) =>
            auditSubmission(GenericAuditDetailOld(request.userDetails, rawData, correlationId, AuditResponse(OK, Right(None))))
        }
      }
    }
  }

  private def auditSubmission(details: GenericAuditDetailOld)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("AmendForeignPropertyIncomeAndExpensesPeriodSummary", "amend-foreign-property-income-and-expenses-period-summary", details)
    auditService.auditEvent(event)
  }

}
