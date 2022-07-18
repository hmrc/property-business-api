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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.AmendForeignPropertyAnnualSubmissionRequestParser
import v2.hateoas.HateoasFactory
import v2.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v2.models.errors._
import v2.models.request.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionRawData
import v2.models.response.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionHateoasData
import v2.models.response.amendForeignPropertyAnnualSubmission.AmendForeignPropertyAnnualSubmissionResponse._
import v2.services.{AmendForeignPropertyAnnualSubmissionService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.{Inject, Singleton}
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
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendForeignPropertyAnnualSubmissionController", endpointName = "AmendForeignPropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")
      val rawData = AmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendForeignPropertyAnnualSubmission(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, AmendForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
              .asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(GenericAuditDetail(request.userDetails, rawData, serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Ok(response)
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        auditSubmission(
          GenericAuditDetail(request.userDetails, rawData, correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | BusinessIdFormatError | TaxYearFormatError | MtdErrorWithCode(RuleBothAllowancesSuppliedError.code) |
          MtdErrorWithCode(RuleBuildingNameNumberError.code) | RuleTypeOfBusinessIncorrectError | MtdErrorWithCode(CountryCodeFormatError.code) |
          MtdErrorWithCode(ValueFormatError.code) | MtdErrorWithCode(DateFormatError.code) | MtdErrorWithCode(StringFormatError.code) |
          MtdErrorWithCode(RuleIncorrectOrEmptyBodyError.code) | RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError | MtdErrorWithCode(
            RuleCountryCodeError.code) | MtdErrorWithCode(RuleDuplicateCountryCodeError.code) | MtdErrorWithCode(
            RulePropertyIncomeAllowanceError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case DownstreamMtdError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendForeignPropertyAnnualSubmission", "create-amend-foreign-property-annual-submission", details)
    auditService.auditEvent(event)
  }
}
