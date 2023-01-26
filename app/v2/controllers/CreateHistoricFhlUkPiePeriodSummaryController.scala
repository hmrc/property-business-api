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

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{ IdGenerator, Logging }
import v2.controllers.requestParsers.CreateHistoricFhlUkPiePeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.audit.{ AuditEvent, AuditResponse, FlattenedGenericAuditDetail }
import v2.models.errors._
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRawData
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryHateoasData
import v2.services.{ AuditService, CreateHistoricFhlUkPiePeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

/**
  * Controller for the Create a Historic UK Furnished Holiday Letting Property Income & Expenses Period Summary endpoint.
  */
@Singleton
class CreateHistoricFhlUkPiePeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                              val lookupService: MtdIdLookupService,
                                                              parser: CreateHistoricFhlUkPiePeriodSummaryRequestParser,
                                                              service: CreateHistoricFhlUkPiePeriodSummaryService,
                                                              auditService: AuditService,
                                                              hateoasFactory: HateoasFactory,
                                                              cc: ControllerComponents,
                                                              idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateHistoricFhlUkPiePeriodSummaryController",
      endpointName = "Create a Historic UK Furnished Holiday Letting Property Income & Expenses Period Summary"
    )

  def handleRequest(nino: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")

      val rawData = CreateHistoricFhlUkPiePeriodSummaryRawData(nino, request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createPeriodSummary(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(
                serviceResponse.responseData,
                CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino, serviceResponse.responseData.periodId)
              )
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            FlattenedGenericAuditDetail(
              versionNumber = Some("2.0"),
              request.userDetails,
              Map("nino" -> nino),
              Some(request.body),
              serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(None))
            )
          )
          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        auditSubmission(
          FlattenedGenericAuditDetail(
            Some("2.0"),
            request.userDetails,
            Map("nino" -> nino),
            Some(request.body),
            resCorrelationId,
            AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case _
          if errorWrapper.containsAnyOf(
            BadRequestError,
            ValueFormatError,
            NinoFormatError,
            FromDateFormatError,
            ToDateFormatError,
            RuleBothExpensesSuppliedError,
            RuleIncorrectOrEmptyBodyError,
            RuleToDateBeforeFromDateError,
            RuleDuplicateSubmissionError,
            RuleMisalignedPeriodError,
            RuleOverlappingPeriodError,
            RuleNotContiguousPeriodError,
            RuleHistoricTaxYearNotSupportedError,
            RuleIncorrectOrEmptyBodyError
          ) =>
        BadRequest(Json.toJson(errorWrapper))

      case UnauthorisedError => Unauthorized(Json.toJson(errorWrapper))
      case NotFoundError     => NotFound(Json.toJson(errorWrapper))
      case InternalError     => InternalServerError(Json.toJson(errorWrapper))
      case _                 => unhandledError(errorWrapper)

    }
  }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("CreateHistoricFhlPropertyIncomeExpensesPeriodSummary", "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary", details)
    auditService.auditEvent(event)
  }
}
