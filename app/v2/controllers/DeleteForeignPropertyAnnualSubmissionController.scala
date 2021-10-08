/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.DeleteForeignPropertyAnnualSubmissionRequestParser
import v2.models.audit.{AuditEvent, AuditResponse, DeleteForeignPropertyAnnualAuditDetail}
import v2.models.errors._
import v2.models.request.deleteForeignPropertyAnnualSubmission.DeleteForeignPropertyAnnualSubmissionRawData
import v2.services.{AuditService, DeleteForeignPropertyAnnualSubmissionService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteForeignPropertyAnnualSubmissionController @Inject()(val authService: EnrolmentsAuthService,
                                                                val lookupService: MtdIdLookupService,
                                                                parser: DeleteForeignPropertyAnnualSubmissionRequestParser,
                                                                service: DeleteForeignPropertyAnnualSubmissionService,
                                                                auditService: AuditService,
                                                                cc: ControllerComponents,
                                                                idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "DeleteForeignPropertyAnnualSubmissionController", endpointName = "deleteForeignPropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = DeleteForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.deleteForeignPropertyAnnualSubmission(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(DeleteForeignPropertyAnnualAuditDetail(request.userDetails, nino, businessId, taxYear,
            serviceResponse.correlationId, AuditResponse(NO_CONTENT, Right(None))))

          NoContent.withApiHeaders(serviceResponse.correlationId)

        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(DeleteForeignPropertyAnnualAuditDetail(request.userDetails, nino, businessId, taxYear,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case NinoFormatError | BusinessIdFormatError | TaxYearFormatError |
           RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError |
           BadRequestError => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: DeleteForeignPropertyAnnualAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("DeleteForeignPropertyAnnualSummary", "Delete-Foreign-Property-Annual-Summary", details)
    auditService.auditEvent(event)
  }
}
