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

package v1.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateForeignPropertyPeriodSummaryRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, CreateForeignPropertyPeriodicAuditDetail}
import v1.models.errors._
import v1.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRawData
import v1.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryHateoasData
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyPeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             parser: CreateForeignPropertyPeriodSummaryRequestParser,
                                                             service: CreateForeignPropertyPeriodSummaryService,
                                                             auditService: AuditService,
                                                             hateoasFactory: HateoasFactory,
                                                             cc: ControllerComponents,
                                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateForeignPropertyController", endpointName = "Create a Foreign Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = CreateForeignPropertyPeriodSummaryRawData(nino, businessId, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createForeignProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, CreateForeignPropertyPeriodSummaryHateoasData(nino, businessId, serviceResponse.responseData.submissionId))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(CreateForeignPropertyPeriodicAuditDetail(request.userDetails, nino, businessId, request.body,
            serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)


        auditSubmission(CreateForeignPropertyPeriodicAuditDetail(request.userDetails, nino, businessId, request.body,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case RuleIncorrectOrEmptyBodyError |
           BadRequestError |
           NinoFormatError |
           BusinessIdFormatError |
           ToDateFormatError |
           FromDateFormatError |
           MtdErrorWithCustomMessage(CountryCodeFormatError.code) |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           MtdErrorWithCustomMessage(RuleBothExpensesSuppliedError.code) |
           RuleToDateBeforeFromDateError |
           MtdErrorWithCustomMessage(RuleCountryCodeError.code) |
           RuleOverlappingPeriodError |
           RuleMisalignedPeriodError |
           RuleNotContiguousPeriodError |
           RuleIncorrectOrEmptyBodyError |
           RuleDuplicateSubmission =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: CreateForeignPropertyPeriodicAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("CreateForeignPropertyIncomeAndExpenditurePeriodSummary", "Create-Foreign-Property-Income-And-Expenditure-Period-Summary", details)
    auditService.auditEvent(event)
  }
}
