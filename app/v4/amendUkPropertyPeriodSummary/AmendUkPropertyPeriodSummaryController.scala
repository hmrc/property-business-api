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

package v4.amendUkPropertyPeriodSummary

import shared.controllers._
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.routing.Version
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendUkPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        validatorFactory: AmendUkPropertyPeriodSummaryValidatorFactory,
                                                        service: AmendUkPropertyPeriodSummaryService,
                                                        auditService: AuditService,
                                                        cc: ControllerComponents,
                                                        idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "amend-uk-property-period-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendUkPropertyController", endpointName = "amendUkProperty")

  def handleRequest(nino: String, businessId: String, taxYear: String, submissionId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, submissionId, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.amendUkPropertyPeriodSummary)
        .withAuditing(AuditHandler(
          auditService,
          "AmendUKPropertyIncomeAndExpensesPeriodSummary",
          "amend-uk-property-income-and-expenses-period-summary",
          Version(request),
          Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "submissionId" -> submissionId),
          Some(request.body)
        ))
        .withNoContentResult(OK)

      requestHandler.handleRequest()
    }

}
