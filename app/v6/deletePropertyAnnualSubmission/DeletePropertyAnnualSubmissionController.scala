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

package v6.deletePropertyAnnualSubmission

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeletePropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                          val lookupService: MtdIdLookupService,
                                                          validatorFactory: DeletePropertyAnnualSubmissionValidatorFactory,
                                                          service: DeletePropertyAnnualSubmissionService,
                                                          auditService: AuditService,
                                                          cc: ControllerComponents,
                                                          idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "delete-property-annual-submission"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "DeletePropertyAnnualSubmissionController", endpointName = "deletePropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.deletePropertyAnnualSubmission)
        .withAuditing(
          AuditHandler(
            auditService,
            auditType = "DeletePropertyAnnualSubmission",
            transactionName = "delete-property-annual-submission",
            apiVersion = Version(request),
            params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear)
          )
        )

      requestHandler.handleRequest()
    }

}
