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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission

import api.controllers._
import api.models.audit.FlattenedGenericAuditDetail
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.{Version, Version4}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteHistoricNonFhlUkPropertyAnnualSubmissionController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: DeleteHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
    service: DeleteHistoricNonFhlUkPropertyAnnualSubmissionService,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "delete-historic-non-fhluk-property-annual-submission"

  private implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteHistoricUkPropertyAnnualSubmissionController",
      endpointName = "deleteHistoricNonFhlUkPropertyAnnualSubmission")

  def handleRequest(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.deleteHistoricUkPropertyAnnualSubmission)
        .withAuditing(AuditHandler.custom(
          auditService,
          auditType = s"DeleteHistoricNonFhlPropertyBusinessAnnualSubmission",
          transactionName = s"delete-uk-property-historic-NonFhl-annual-submission",
          auditDetailCreator = FlattenedGenericAuditDetail.auditDetailCreator(
            Version.from(request, orElse = Version4),
            Map("nino" -> nino, "taxYear" -> taxYear)
          ),
          requestBody = None,
          responseBodyMap = None => None
        ))

      requestHandler.handleRequest()
    }

}
