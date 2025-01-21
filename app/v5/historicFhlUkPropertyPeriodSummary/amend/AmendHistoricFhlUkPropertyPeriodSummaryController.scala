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

package v5.historicFhlUkPropertyPeriodSummary.amend

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
class AmendHistoricFhlUkPropertyPeriodSummaryController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory,
    service: AmendHistoricFhlUkPropertyPeriodSummaryService,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "amend-historic-fhluk-property-period-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendHistoricFhlUkPropertyPeriodSummaryController",
      endpointName = "AmendHistoricFhlUkPropertyPeriodSummary"
    )

  def handleRequest(nino: String, periodId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, periodId, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "AmendHistoricFhlPropertyIncomeExpensesPeriodSummary",
              transactionName = "amend-historic-fhl-property-income-expenses-period-summary",
              apiVersion = Version(request),
              params = Map("nino" -> nino, "periodId" -> periodId),
              requestBody = Some(request.body)
            )
          )
          .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
