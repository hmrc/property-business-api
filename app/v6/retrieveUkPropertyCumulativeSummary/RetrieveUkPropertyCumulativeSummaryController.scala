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

package v6.retrieveUkPropertyCumulativeSummary

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import shared.services.{EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveUkPropertyCumulativeSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               validatorFactory: RetrieveUkPropertyCumulativeSummaryValidatorFactory,
                                                               service: RetrieveUkPropertyCumulativeSummaryService,
                                                               cc: ControllerComponents,
                                                               idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "retrieve-uk-property-cumulative-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveUkPropertyCumulativeSummaryController", endpointName = endpointName)

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveUkProperty)
          .withPlainJsonResult(OK)

      requestHandler.handleRequest()
    }

}
