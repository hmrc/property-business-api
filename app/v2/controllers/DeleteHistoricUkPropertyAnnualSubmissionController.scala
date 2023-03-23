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

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.models.audit.AuditResponse
import cats.data.EitherT
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.DeleteHistoricUkPropertyAnnualSubmissionRequestParser
import v2.models.domain.HistoricPropertyType
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import v2.models.audit.{AuditEvent, FlattenedGenericAuditDetail}
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
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

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

          auditSubmission(
            s"${propertyType.toString}",
            FlattenedGenericAuditDetail(
              versionNumber = Some("2.0"),
              request.userDetails,
              Map("nino" -> nino, "taxYear" -> taxYear),
              None,
              serviceResponse.correlationId,
              AuditResponse(httpStatus = NO_CONTENT, response = Right(None))
            )
          )

          NoContent.withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          s"${propertyType.toString}",
          FlattenedGenericAuditDetail(
            versionNumber = Some("2.0"),
            request.userDetails,
            Map("nino" -> nino, "taxYear" -> taxYear),
            None,
            resCorrelationId,
            AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext) =
    errorWrapper.error match {
      case _
          if errorWrapper.containsAnyOf(
            NinoFormatError,
            TaxYearFormatError,
            RuleHistoricTaxYearNotSupportedError,
            RuleTaxYearRangeInvalidError,
            BadRequestError,
            RuleIncorrectGovTestScenarioError
          ) =>
        BadRequest(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case _             => unhandledError(errorWrapper)
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
