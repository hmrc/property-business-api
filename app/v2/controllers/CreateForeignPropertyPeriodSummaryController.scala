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
import cats.implicits._
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{ IdGenerator, Logging }
import v2.controllers.requestParsers.CreateForeignPropertyPeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.audit.{ AuditEvent, AuditResponse, GenericAuditDetail }
import v2.models.errors._
import v2.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRawData
import v2.models.response.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryHateoasData
import v2.services._

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CreateForeignPropertyPeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             parser: CreateForeignPropertyPeriodSummaryRequestParser,
                                                             service: CreateForeignPropertyPeriodSummaryService,
                                                             auditService: AuditService,
                                                             hateoasFactory: HateoasFactory,
                                                             cc: ControllerComponents,
                                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateForeignPropertyPeriodSummaryController",
                       endpointName = "Create a Foreign Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")
      val rawData = CreateForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, body = request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createForeignProperty(parsedRequest))
        } yield {
          val hateoasData = CreateForeignPropertyPeriodSummaryHateoasData(
            nino = nino,
            businessId = businessId,
            taxYear = taxYear,
            submissionId = serviceResponse.responseData.submissionId
          )

          val vendorResponse = hateoasFactory.wrap(serviceResponse.responseData, hateoasData)

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(
            GenericAuditDetail(request.userDetails, rawData, serviceResponse.correlationId, AuditResponse(CREATED, Right(Some(response)))))

          Created(Json.toJson(vendorResponse))
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
      case _
          if errorWrapper.containsAnyOf(
            BadRequestError,
            NinoFormatError,
            TaxYearFormatError,
            BusinessIdFormatError,
            RuleTaxYearRangeInvalidError,
            RuleTaxYearNotSupportedError,
            RuleIncorrectOrEmptyBodyError,
            ToDateFormatError,
            FromDateFormatError,
            ValueFormatError,
            RuleBothExpensesSuppliedError,
            RuleToDateBeforeFromDateError,
            RuleOverlappingPeriodError,
            RuleMisalignedPeriodError,
            RuleNotContiguousPeriodError,
            RuleIncorrectOrEmptyBodyError,
            RuleDuplicateSubmissionError,
            CountryCodeFormatError,
            RuleCountryCodeError,
            RuleDuplicateCountryCodeError,
            RuleTypeOfBusinessIncorrectError
          ) =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _             => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event =
      AuditEvent("CreateForeignPropertyIncomeAndExpensesPeriodSummary", "create-foreign-property-income-and-expenses-period-summary", details)
    auditService.auditEvent(event)
  }
}
