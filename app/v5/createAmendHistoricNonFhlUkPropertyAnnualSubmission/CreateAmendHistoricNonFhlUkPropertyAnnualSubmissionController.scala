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

package v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.models.audit.GenericAuditDetail
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
    service: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-amend-historic-non-fhluk-property-annual-submission"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController",
      endpointName = "CreateAmendHistoricNonFhlUkPropertyAnnualSubmission")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withAuditing(
            AuditHandler.custom(
              auditService,
              auditType = "CreateAndAmendHistoricNonFhlPropertyBusinessAnnualSubmission",
              transactionName = "create-and-amend-historic-non-fhl-property-business-annual-submission",
              auditDetailCreator = GenericAuditDetail.auditDetailCreator(
                Version(request),
                Map("nino" -> nino, "taxYear" -> taxYear)
              ),
              requestBody = Some(request.body),
              responseBodyMap = None => None
            )
          )
          .withNoContentResult(OK)

      requestHandler.handleRequest()

    }

}
