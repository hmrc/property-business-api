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

package v1.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendForeignPropertyAnnualSubmissionRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, CreateAndAmendForeignPropertyAnnualAuditDetail}
import v1.models.errors._
import v1.models.request.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionRawData
import v1.models.response.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionResponse.AmendForeignPropertyLinksFactory
import v1.models.response.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionHateoasData
import v1.services.{AmendForeignPropertyAnnualSubmissionService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendForeignPropertyAnnualSubmissionController @Inject()(val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               parser: AmendForeignPropertyAnnualSubmissionRequestParser,
                                                               service: AmendForeignPropertyAnnualSubmissionService,
                                                               auditService: AuditService,
                                                               hateoasFactory: HateoasFactory,
                                                               cc: ControllerComponents,
                                                               idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendForeignPropertyAnnualSubmissionController", endpointName = "AmendForeignPropertyAnnualSubmission")
  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = AmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendForeignPropertyAnnualSubmission(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response recieved with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(CreateAndAmendForeignPropertyAnnualAuditDetail(request.userDetails, nino, businessId, taxYear, request.body,
            serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(CreateAndAmendForeignPropertyAnnualAuditDetail(request.userDetails, nino, businessId, taxYear, request.body,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    (errorWrapper.error: @unchecked) match {
      case BadRequestError |
           NinoFormatError |
           BusinessIdFormatError |
           TaxYearFormatError |
           MtdErrorWithCustomMessage(CountryCodeFormatError.code) |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           RuleIncorrectOrEmptyBodyError |
           RuleTaxYearNotSupportedError |
           RuleTaxYearRangeInvalidError |
           MtdErrorWithCustomMessage(RuleCountryCodeError.code) => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
    }
  }
  private def auditSubmission(details: CreateAndAmendForeignPropertyAnnualAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendForeignPropertyAnnualSummary", "Create-Amend-Foreign-Property-Annual-Summary", details)
    auditService.auditEvent(event)
  }
}
