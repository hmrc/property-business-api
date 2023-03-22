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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.ListHistoricUkPropertyPeriodSummariesRequestParser
import v2.hateoas.HateoasFactory
import v2.models.domain.HistoricPropertyType
import v2.models.errors._
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRawData
import v2.models.response.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesHateoasData
import v2.services.{EnrolmentsAuthService, ListHistoricUkPropertyPeriodSummariesService, MtdIdLookupService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListHistoricUkPropertyPeriodSummariesController @Inject() (val authService: EnrolmentsAuthService,
                                                                 val lookupService: MtdIdLookupService,
                                                                 service: ListHistoricUkPropertyPeriodSummariesService,
                                                                 parser: ListHistoricUkPropertyPeriodSummariesRequestParser,
                                                                 hateoasFactory: HateoasFactory,
                                                                 cc: ControllerComponents,
                                                                 idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  def handleFhlRequest(nino: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(
        controllerName = "ListHistoricUkPropertyPeriodSummariesController",
        endpointName = "ListHistoricFhlUkPropertyPeriodSummariesController")

    handleRequest(nino, HistoricPropertyType.Fhl)
  }

  def handleNonFhlRequest(nino: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
      controllerName = "ListHistoricUkPropertyPeriodSummariesController",
      endpointName = "ListHistoricNonFhlUkPropertyPeriodSummariesController")
    handleRequest(nino, HistoricPropertyType.NonFhl)
  }

  private def handleRequest(nino: String, propertyType: HistoricPropertyType)(implicit endpointLogContext: EndpointLogContext): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")
      val rawData = ListHistoricUkPropertyPeriodSummariesRawData(nino)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.listPeriodSummaries(parsedRequest, propertyType))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrapList(serviceResponse.responseData, ListHistoricUkPropertyPeriodSummariesHateoasData(nino, propertyType))
              .asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
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

  private def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext) =
    errorWrapper.error match {
      case _ if errorWrapper.containsAnyOf(NinoFormatError, BadRequestError, RuleIncorrectGovTestScenarioError) =>
        BadRequest(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _             => unhandledError(errorWrapper)
    }

}
