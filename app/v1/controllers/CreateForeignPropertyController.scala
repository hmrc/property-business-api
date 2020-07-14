/*
 * Copyright 2020 HM Revenue & Customs
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
import utils.Logging
import v1.controllers.requestParsers.CreateForeignPropertyRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.createForeignProperty.CreateForeignPropertyRawData
import v1.models.response.createForeignProperty.CreateForeignPropertyHateoasData
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateForeignPropertyController @Inject()(val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                parser: CreateForeignPropertyRequestParser,
                                                service: CreateForeignPropertyService,
                                                hateoasFactory: HateoasFactory,
                                                cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateForeignPropertyController", endpointName = "Create a Foreign Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = CreateForeignPropertyRawData(nino, businessId, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createForeignProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, CreateForeignPropertyHateoasData(nino, businessId, serviceResponse.responseData.submissionId))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
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
           RuleIncorrectOrEmptyBodyError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

}