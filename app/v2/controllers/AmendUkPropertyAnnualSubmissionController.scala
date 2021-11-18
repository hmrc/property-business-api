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
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.AmendUkPropertyAnnualSubmissionRequestParser
import v2.hateoas.HateoasFactory
import v2.models.errors._
import v2.models.request.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionRawData
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionHateoasData
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionResponse.AmendUkPropertyLinksFactory
import v2.services.{AmendUkPropertyAnnualSubmissionService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendUkPropertyAnnualSubmissionController @Inject()(val authService: EnrolmentsAuthService,
                                                          val lookupService: MtdIdLookupService,
                                                          parser: AmendUkPropertyAnnualSubmissionRequestParser,
                                                          service: AmendUkPropertyAnnualSubmissionService,
                                                          hateoasFactory: HateoasFactory,
                                                          cc: ControllerComponents,
                                                          idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendUkPropertyAnnualSubmissionController", endpointName = "AmendUkPropertyAnnualSubmission")
  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] with correlationId : $correlationId")
      val rawData = AmendUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendUkPropertyAnnualSubmission(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendUkPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear)).asRight[ErrorWrapper])
        } yield {

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response recieved with CorrelationId: ${serviceResponse.correlationId}")

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
           TaxYearFormatError |
           BusinessIdFormatError |
           RuleTaxYearRangeInvalidError |
           RuleTaxYearNotSupportedError |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           MtdErrorWithCustomMessage(RuleIncorrectOrEmptyBodyError.code) |
           RuleTypeOfBusinessIncorrectError |
           RuleBothAllowancesSuppliedError |
           RuleBuildingNameNumberError |
           MtdErrorWithCustomMessage(DateFormatError.code) |
           MtdErrorWithCustomMessage(StringFormatError.code) => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
    }
  }
}
