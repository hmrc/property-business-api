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

package v5.deleteHistoricFhlUkPropertyAnnualSubmission

import common.models.domain.HistoricPropertyType
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.models.audit.GenericAuditDetail
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteHistoricFhlUkPropertyAnnualSubmissionController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory,
    service: DeleteHistoricFhlUkPropertyAnnualSubmissionService,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "delete-historic-fhluk-property-annual-submission"

  def handleFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext = {
      EndpointLogContext(
        controllerName = "DeleteHistoricFhlUkPropertyAnnualSubmissionController",
        endpointName = "deleteHistoricFhlUkPropertyAnnualSubmission")
    }

    handleRequest(nino, taxYear, HistoricPropertyType.Fhl)
  }

  def handleNonFhlRequest(nino: String, taxYear: String): Action[AnyContent] = {
    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(
        controllerName = "DeleteHistoricFhlUkPropertyAnnualSubmissionController",
        endpointName = "deleteHistoricNonFhlUkPropertyAnnualSubmission")

    handleRequest(nino, taxYear, HistoricPropertyType.NonFhl)
  }

  def handleRequest(nino: String, taxYear: String, propertyType: HistoricPropertyType)(implicit
      endpointLogContext: EndpointLogContext): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, propertyType)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.deleteHistoricUkPropertyAnnualSubmission)
        .withAuditing(AuditHandler.custom(
          auditService,
          auditType = s"DeleteHistoric${propertyType}PropertyBusinessAnnualSubmission",
          transactionName = s"delete-uk-property-historic-$propertyType-annual-submission",
          auditDetailCreator = GenericAuditDetail.auditDetailCreator(
            Version(request),
            Map("nino" -> nino, "taxYear" -> taxYear)
          ),
          requestBody = None,
          responseBodyMap = None => None
        ))

      requestHandler.handleRequest()
    }

}
