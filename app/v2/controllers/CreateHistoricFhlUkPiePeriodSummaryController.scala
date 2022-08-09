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
import cats.implicits.catsSyntaxEitherId
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents }
import utils.{ IdGenerator, Logging }
import v2.controllers.requestParsers.CreateHistoricFhlUkPiePeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.errors._
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRawData
import v2.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryHateoasData
import v2.services.{ CreateHistoricFhlUkPiePeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService }

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
                CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino,
                                                               s"${parsedRequest.body.fromDate}_${parsedRequest.body.toDate}",
                                                               serviceResponse.responseData.transactionReference)
              )
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

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
            RuleTaxYearNotSupportedError,
            RuleIncorrectOrEmptyBodyError
          ) =>
        BadRequest(Json.toJson(errorWrapper))

      case UnauthorisedError => Unauthorized(Json.toJson(errorWrapper))
      case NotFoundError     => NotFound(Json.toJson(errorWrapper))
      case InternalError     => InternalServerError(Json.toJson(errorWrapper))
      case _                 => unhandledError(errorWrapper)

    }
  }
}
