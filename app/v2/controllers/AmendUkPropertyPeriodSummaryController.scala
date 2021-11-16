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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.AmendUkPropertyPeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.audit.{AuditEvent, AuditResponse}
import v2.models.errors._
import v2.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendUkPropertyPeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                       val lookupService: MtdIdLookupService,
                                                       parser: AmendUkPropertyPeriodSummaryRequestParser,
                                                       service: AmendUkPropertyPeriodSummaryService,
                                                       auditService: AuditService,
                                                       hateoasFactory: HateoasFactory,
                                                       cc: ControllerComponents,
                                                       idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendUkPropertyController", endpointName = "amendUkProperty")
  def handleRequest(nino: String, taxYear: String, businessId: String, submissionId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
        val rawData = AmendUkPropertyPeriodSummaryRawData(nino, businessId, submissionId, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendUkPropertyPeriodSummary(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendUkPropertyPeriodSummaryHateoasData(nino, taxYear, businessId, submissionId)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(AmendUkPropertyPeriodicAuditDetail(request.userDetails, nino, businessId, submissionId, request.body,
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


        auditSubmission(AmendUkPropertyPeriodicAuditDetail(request.userDetails, nino, businessId, submissionId, request.body,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError |
           NinoFormatError |
           BusinessIdFormatError |
           SubmissionIdFormatError |
           RuleTaxYearRangeInvalidError |
           RuleTaxYearNotSupportedError |
           RuleIncorrectOrEmptyBodyError |
           RuleTypeOfBusinessIncorrect|
           RuleBothExpensesSuppliedError|
           MtdErrorWithCustomMessage(CountryCodeFormatError.code) |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           MtdErrorWithCustomMessage(RuleBothExpensesSuppliedError.code) |
           MtdErrorWithCustomMessage(RuleCountryCodeError.code) |
           MtdErrorWithCustomMessage(RuleIncorrectOrEmptyBodyError.code) |
           RuleDuplicateSubmission =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: AmendUkPropertyPeriodicAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("AmendUkPropertyIncomeAndExpenditurePeriodSummary", "Amend-Uk-Property-Income-And-Expenditure-Period-Summary", details)
    auditService.auditEvent(event)
  }
}