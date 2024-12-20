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

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.routing.Version
import utils.IdGenerator
import v3.controllers.validators.CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
import v3.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission.CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData
import v3.services.CreateAmendHistoricFhlUkPropertyAnnualSubmissionService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory,
    service: CreateAmendHistoricFhlUkPropertyAnnualSubmissionService,
    auditService: AuditService,
    hateoasFactory: HateoasFactory,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-amend-historic-fhluk-property-annual-submission"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendHistoricFhlUkPropertyAnnualSubmissionController",
      endpointName = "CreateAmendHistoricFhlUkPropertyAnnualSubmission")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
              transactionName = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
              apiVersion = Version(request),
              params = Map("nino" -> nino, "taxYear" -> taxYear),
              requestBody = Some(request.body)
            )
          )
          .withHateoasResult(hateoasFactory)(CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))

      requestHandler.handleRequest()
    }

}
