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

package v6.createAmendUkPropertyCumulativeSummary

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendUkPropertyCumulativeSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                  val lookupService: MtdIdLookupService,
                                                                  validatorFactory: CreateAmendUkPropertyCumulativeSummaryValidatorFactory,
                                                                  service: CreateAmendUkPropertyCumulativeSummaryService,
                                                                  auditService: AuditService,
                                                                  cc: ControllerComponents,
                                                                  idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-amend-uk-property-cumulative-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAmendUkPropertyCumulativeSummaryController", endpointName = endpointName)

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAmendUkPropertyCumulativeSummary)
        .withAuditing(AuditHandler(
          auditService,
          "CreateAmendUkPropertyCumulativeSummary",
          "create-amend-uk-property-cumulative-summary",
          Version(request),
          Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
          Some(request.body)
        ))
        .withNoContentResult(NO_CONTENT)

      requestHandler.handleRequest()
    }

}
