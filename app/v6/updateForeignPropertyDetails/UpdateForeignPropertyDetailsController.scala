/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.updateForeignPropertyDetails

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.*
import shared.routing.Version
import shared.services.*
import shared.utils.IdGenerator

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UpdateForeignPropertyDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        validatorFactory: UpdateForeignPropertyDetailsValidatorFactory,
                                                        service: UpdateForeignPropertyDetailsService,
                                                        auditService: AuditService,
                                                        cc: ControllerComponents,
                                                        idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "update-foreign-property-details"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "UpdateForeignPropertyDetailsController",
      endpointName = "Update Foreign Property Details"
    )

  def handleRequest(nino: String, propertyId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, propertyId, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.updateForeignPropertyDetails)
          .withAuditing(AuditHandler(
            auditService,
            "UpdateForeignPropertyDetails",
            "update-foreign-property-details",
            Version(request),
            Map("nino" -> nino, "propertyId" -> propertyId, "taxYear" -> taxYear),
            Some(request.body)
          ))
          .withNoContentResult()

      requestHandler.handleRequest()
    }

}
