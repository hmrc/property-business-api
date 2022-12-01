/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v2.models.audit.AuditResponse
import v2.controllers.requestParsers.DeleteHistoricUkPropertyAnnualSubmissionRequestParser
import v2.models.domain.HistoricPropertyType
import v2.models.errors._
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRawData
import v2.services.{AuditService, DeleteHistoricUkPropertyAnnualSubmissionService, EnrolmentsAuthService, MtdIdLookupService}
import v2.models.audit.{AuditEvent, AuditResponse, DeleteUkPropertyAnnualSubmissionAuditDetail}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionController @Inject()(val authService: EnrolmentsAuthService,
                                                                   val lookupService: MtdIdLookupService,
                                                                   parser: DeleteHistoricUkPropertyAnnualSubmissionRequestParser,
                                                                   service: DeleteHistoricUkPropertyAnnualSubmissionService,
                                                                   auditService: AuditService,
                                                                   cc: ControllerComponents,
                                                                   idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  def handleFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext = {
      EndpointLogContext(controllerName = "DeleteHistoricUkPropertyAnnualSubmissionController",
                         endpointName = "deleteHistoricFhlUkPropertyAnnualSubmission")
    }

    handleRequest(nino, taxYear, HistoricPropertyType.Fhl)
  }

  def handleNonFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(controllerName = "DeleteHistoricUkPropertyAnnualSubmissionController",
                         endpointName = "deleteHistoricNonFhlUkPropertyAnnualSubmission")

    handleRequest(nino, taxYear, HistoricPropertyType.NonFhl)
  }

  def handleRequest(nino: String, taxYear: String, propertyType: HistoricPropertyType)(
      implicit endpointLogContext: EndpointLogContext): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")
      val rawData = DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.deleteHistoricUkPropertyAnnualSubmission(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
          auditSubmission(DeleteUkPropertyAnnualSubmissionAuditDetail(request.userDetails,
            nino,
            taxYear = taxYear,
            `X-CorrelationId` = correlationId, response = AuditResponse(NO_CONTENT, Right(None))))

          NoContent.withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(DeleteUkPropertyAnnualSubmissionAuditDetail(
          request.userDetails, nino,taxYear, correlationId,  AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext) =
    errorWrapper.error match {
      case _
          if errorWrapper.containsAnyOf(NinoFormatError,
                                        TaxYearFormatError,
                                        RuleHistoricTaxYearNotSupportedError,
                                        RuleTaxYearRangeInvalidError,
                                        BadRequestError) =>
        BadRequest(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case _             => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: DeleteUkPropertyAnnualSubmissionAuditDetail)
                             (implicit endpointLogContext:EndpointLogContext,  hc: HeaderCarrier, ec: ExecutionContext):Unit ={
    val propertyType: String = endpointLogContext.endpointName match {
      case "deleteHistoricFhlUkPropertyAnnualSubmission" => "Fhl"
      case _ => "NonFhl"
    }
    val auditType: String = s"DeleteHistoric${propertyType}PropertyBusinessAnnualSubmission"
    val transactionName: String = s"DeleteHistoric${propertyType}PropertyBusinessAnnualSubmission"
    val event = AuditEvent(auditType,transactionName, details)
    auditService.auditEvent(event)
  }
}
