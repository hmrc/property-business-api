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
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{ IdGenerator, Logging }
import v2.controllers.requestParsers.AmendHistoricNonFhlUkPiePeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.errors._
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryRawData
import v2.models.response.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData
import v2.services.{ AmendHistoricNonFhlUkPiePeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService, AuditService }
import v2.models.audit.{ AuditEvent, AuditResponse, FlattenedGenericAuditDetail}

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AmendHistoricNonFhlUkPropertyPeriodSummaryController @Inject()(
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    parser: AmendHistoricNonFhlUkPiePeriodSummaryRequestParser,
    service: AmendHistoricNonFhlUkPiePeriodSummaryService,
    hateoasFactory: HateoasFactory,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator
)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(controllerName = "AmendHistoricNonFhlUkPropertyPeriodSummaryController",
                                                                           endpointName = "AmendHistoricNonFhlUkPropertyPeriodSummary")

  def handleRequest(nino: String, periodId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId

      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] with correlationId : $correlationId")

      val rawData = AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, request.body)

      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
        } yield {

          val vendorResponse = hateoasFactory
            .wrap(serviceResponse.responseData, AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData(nino, periodId))

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            FlattenedGenericAuditDetail(
              versionNumber = Some("2.0"),
              request.userDetails,
              Map("nino" -> nino, "periodId" -> periodId),
              Some(request.body),
              serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(None))
            )
          )

          val response = Json.toJson(vendorResponse)

          Ok(response).withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          FlattenedGenericAuditDetail(
            versionNumber = Some("2.0"),
            request.userDetails,
            Map("nino" -> nino, "periodId" -> periodId),
            Some(request.body),
            resCorrelationId,
            AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = errorWrapper.error match {
    case _
        if errorWrapper.containsAnyOf(
          BadRequestError,
          NinoFormatError,
          PeriodIdFormatError,
          RuleBothExpensesSuppliedError,
          ValueFormatError,
          RuleIncorrectOrEmptyBodyError,
          RuleIncorrectGovTestScenarioError
        ) =>
      BadRequest(Json.toJson(errorWrapper))
    case NotFoundError => NotFound(Json.toJson(errorWrapper))
    case InternalError => InternalServerError(Json.toJson(errorWrapper))
    case _             => unhandledError(errorWrapper)
  }
  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary", "AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary", details)
    auditService.auditEvent(event)
  }}
