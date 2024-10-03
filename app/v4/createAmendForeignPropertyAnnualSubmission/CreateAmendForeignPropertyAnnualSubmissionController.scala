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

package v4.createAmendForeignPropertyAnnualSubmission

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version3}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                                      val lookupService: MtdIdLookupService,
                                                                      validatorFactory: CreateAmendForeignPropertyAnnualSubmissionValidatorFactory,
                                                                      service: CreateAmendForeignPropertyAnnualSubmissionService,
                                                                      auditService: AuditService,
                                                                      cc: ControllerComponents,
                                                                      idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-amend-foreign-property-annual-submission"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendForeignPropertyAnnualSubmissionController",
      endpointName = "CreateAmendForeignPropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAmendForeignPropertyAnnualSubmission)
        .withAuditing(
          AuditHandler(
            auditService,
            "CreateAmendForeignPropertyAnnualSubmission",
            "create-amend-foreign-property-annual-submission",
            Version.from(request, orElse = Version3),
            params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
            requestBody = Some(request.body)
          )
        )
        .withNoContentResult(OK)

      requestHandler.handleRequest()
    }

}
