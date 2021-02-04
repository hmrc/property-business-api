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
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendForeignPropertyPeriodSummaryRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryRawData
import v1.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryHateoasData
import v1.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryResponse.AmendForeignPropertyLinksFactory
import v1.services.{AmendForeignPropertyPeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendForeignPropertyPeriodSummaryController  @Inject()(val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             parser: AmendForeignPropertyPeriodSummaryRequestParser,
                                                             service: AmendForeignPropertyPeriodSummaryService,
                                                             hateoasFactory: HateoasFactory,
                                                             cc: ControllerComponents,
                                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendForeignPropertyController", endpointName = "amendForeignProperty")
  def handleRequest(nino: String, businessId: String, submissionId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = AmendForeignPropertyPeriodSummaryRawData(nino, businessId, submissionId, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendForeignProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, submissionId)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    (errorWrapper.error: @unchecked) match {
      case BadRequestError |
           NinoFormatError |
           BusinessIdFormatError |
           SubmissionIdFormatError |
           MtdErrorWithCustomMessage(CountryCodeFormatError.code) |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           RuleIncorrectOrEmptyBodyError |
           MtdErrorWithCustomMessage(RuleBothExpensesSuppliedError.code) |
           MtdErrorWithCustomMessage(RuleCountryCodeError.code) => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError | SubmissionIdNotFoundError => NotFound(Json.toJson(errorWrapper))
    }
  }
}
