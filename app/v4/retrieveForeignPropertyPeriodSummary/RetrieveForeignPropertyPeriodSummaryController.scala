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

package v4.retrieveForeignPropertyPeriodSummary

import shared.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import shared.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveForeignPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                val lookupService: MtdIdLookupService,
                                                                validatorFactory: RetrieveForeignPropertyPeriodSummaryValidatorFactory,
                                                                service: RetrieveForeignPropertyPeriodSummaryService,
                                                                cc: ControllerComponents,
                                                                idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "retrieve-foreign-property-period-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveForeignPropertyController", endpointName = "retrieveForeignProperty")

  def handleRequest(nino: String, businessId: String, taxYear: String, submissionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, submissionId)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveForeignProperty)
          .withPlainJsonResult(OK)

      requestHandler.handleRequest()
    }

}
