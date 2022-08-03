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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.CreateUkPropertyPeriodSummaryRequestParser
import v2.hateoas.HateoasFactory
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRawData
import v2.services.{AuditService, CreateUkPropertyPeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                              val lookupService: MtdIdLookupService,
                                                              parser: CreateUkPropertyPeriodSummaryRequestParser, //TODO: Update Parser
                                                              service: CreateUkPropertyPeriodSummaryService, //TODO: Update service
                                                              auditService: AuditService,
                                                              hateoasFactory: HateoasFactory,
                                                              cc: ControllerComponents,
                                                              idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateHistoricFhlUkPiePeriodSummaryController",
      endpointName = "Create a Historic UK Furnished Holiday Letting Property Income & Expenses Period Summary")
  def handleRequest(nino: String): Action[JsValue] = {
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")

      val rawData = CreateHistoricFhlUkPiePeriodSummaryRawData(nino, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createUkProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap((serviceResponse.responseData,
                ))
          )
        }
    }
  }

  private def errorResult()={
    ???
  }
}
