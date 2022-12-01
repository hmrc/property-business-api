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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
import v2.hateoas.HateoasFactory
import v2.models.audit.GenericAuditDetail.FlattenedGenericAuditDetail
import v2.models.audit.{AuditEvent, AuditResponse}
import v2.models.errors._
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData
import v2.services.{AuditService, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService, EnrolmentsAuthService, MtdIdLookupService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController @Inject()(
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    parser: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser,
    service: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService,
    auditService: AuditService,
    hateoasFactory: HateoasFactory,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext = {
    EndpointLogContext(controllerName = "CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController",
      endpointName = "CreateAmendHistoricNonFhlUkPropertyAnnualSubmission")
  }

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId

      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] with correlationId : $correlationId")

      val rawData = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))
              .asRight[ErrorWrapper])
        } yield {

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}"
          )

          auditSubmission(
            FlattenedGenericAuditDetail(
              versionNumber = Some("2.0"),
              request.userDetails,
              Map("nino" -> nino, "taxYear" -> taxYear),
              Some(request.body),
              serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(None))
            )
          )


          val response = Json.toJson(vendorResponse)

          Ok(response)
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          FlattenedGenericAuditDetail(
            Some("2.0"),
            request.userDetails,
            Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body),
            resCorrelationId,
            AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case _
        if errorWrapper.containsAnyOf(
          BadRequestError,
          NinoFormatError,
          TaxYearFormatError,
          RuleTaxYearRangeInvalidError,
          RuleHistoricTaxYearNotSupportedError,
          ValueFormatError,
          RuleIncorrectOrEmptyBodyError
        ) =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }
  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAndAmendHistoricNonFhlUkPropertyAnnualSubmission", "CreateAndAmendHistoricNonFhlUkPropertyAnnualSubmission", details)
    auditService.auditEvent(event)
  }
}
