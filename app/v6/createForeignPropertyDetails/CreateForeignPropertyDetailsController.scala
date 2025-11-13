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

package v6.createForeignPropertyDetails

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.*
import shared.controllers.validators.Validator
import shared.routing.Version
import shared.services.*
import shared.utils.IdGenerator
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateForeignPropertyDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        validatorFactory: CreateForeignPropertyDetailsValidatorFactory,
                                                        service: CreateForeignPropertyDetailsService,
                                                        auditService: AuditService,
                                                        cc: ControllerComponents,
                                                        idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-foreign-property-details"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateForeignPropertyDetailsController",
      endpointName = "Create Foreign Property Details"
    )

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator: Validator[CreateForeignPropertyDetailsRequestData] = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.createForeignPropertyDetails)
          .withAuditing(AuditHandler(
            auditService,
            "CreateForeignPropertyDetails",
            "create-foreign-property-details",
            Version(request),
            Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
            Some(request.body),
            includeResponse = true
          ))
          .withPlainJsonResult()

      requestHandler.handleRequest()
    }

}
