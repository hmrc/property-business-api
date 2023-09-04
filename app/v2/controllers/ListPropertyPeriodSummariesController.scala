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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandlerOld}
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v2.controllers.requestParsers.ListPropertyPeriodSummariesRequestParser
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRawData
import v2.models.response.listPropertyPeriodSummaries.ListPropertyPeriodSummariesHateoasData
import v2.services.ListPropertyPeriodSummariesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListPropertyPeriodSummariesController @Inject() (val authService: EnrolmentsAuthService,
                                                       val lookupService: MtdIdLookupService,
                                                       service: ListPropertyPeriodSummariesService,
                                                       parser: ListPropertyPeriodSummariesRequestParser,
                                                       hateoasFactory: HateoasFactory,
                                                       cc: ControllerComponents,
                                                       idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPropertyPeriodSummariesController", endpointName = "listPropertyPeriodSummaries")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = ListPropertyPeriodSummariesRawData(nino, businessId, taxYear)

      val requestHandler =
        RequestHandlerOld
          .withParser(parser)
          .withService(service.listPeriodSummaries)
          .withHateoasResult(hateoasFactory)(ListPropertyPeriodSummariesHateoasData(nino, businessId, taxYear))

      requestHandler.handleRequest(rawData)
    }

}
