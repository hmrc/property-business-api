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

package v3.controllers

import api.controllers.ResultCreator.hateoasListWrapping
import api.controllers._
import api.hateoas.HateoasFactory
import api.models.domain.HistoricPropertyType
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v3.controllers.validators.ListHistoricUkPropertyPeriodSummariesValidatorFactory
import v3.models.response.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesHateoasData
import v3.services.ListHistoricUkPropertyPeriodSummariesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListHistoricUkPropertyPeriodSummariesController @Inject() (val authService: EnrolmentsAuthService,
                                                                 val lookupService: MtdIdLookupService,
                                                                 service: ListHistoricUkPropertyPeriodSummariesService,
                                                                 validatorFactory: ListHistoricUkPropertyPeriodSummariesValidatorFactory,
                                                                 hateoasFactory: HateoasFactory,
                                                                 cc: ControllerComponents,
                                                                 idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "list-historic-uk-property-period-summaries"

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
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService({ req =>
          service.listPeriodSummaries(req, propertyType)
        })
        .withResultCreator(hateoasListWrapping(hateoasFactory)((_, _) => ListHistoricUkPropertyPeriodSummariesHateoasData(nino, propertyType)))

      requestHandler.handleRequest()

    }

}
