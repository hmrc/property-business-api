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

package v1.controllers.foreignPropertyPeriodSummary

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.Logging
import v1.controllers.requestParsers.foreignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequestParser
import v1.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.foreignPropertyPeriodSummary.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRawData
import v1.models.response.foreignPropertyPeriodSummary.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyHateoasData
import v1.services.foreignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryService
import v1.services.{EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyPeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               parser: RetrieveForeignPropertyPeriodSummaryRequestParser,
                                                               service: RetrieveForeignPropertyPeriodSummaryService,
                                                               hateoasFactory: HateoasFactory,
                                                               cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveForeignPropertyController", endpointName = "retrieveForeignProperty")

  def handleRequest(nino: String, businessId: String, submissionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      val rawData = RetrieveForeignPropertyPeriodSummaryRawData(nino, businessId, submissionId)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieveForeignProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, RetrieveForeignPropertyHateoasData(nino, businessId, submissionId)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)

        }
      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        errorResult(errorWrapper).withApiHeaders(correlationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case NinoFormatError | BusinessIdFormatError | SubmissionIdFormatError | BadRequestError => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError | SubmissionIdNotFoundError => NotFound(Json.toJson(errorWrapper))
    }
  }
}
