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

package v4.createHistoricFhlUkPropertyPeriodSummary

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version4}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateHistoricFhlUkPropertyPeriodSummaryController @Inject()(
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    validatorFactory: CreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory,
    service: CreateHistoricFhlUkPropertyPeriodSummaryService,
    auditService: AuditService,
    cc: ControllerComponents,
    idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateHistoricFhlUkPropertyPeriodSummaryController",
      endpointName = "CreateHistoricFhlUkPropertyPeriodSummary"
    )

  def handleRequest(nino: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.create)
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
              transactionName = "create-historic-fhl-property-income-expenses-period-summary",
              apiVersion = Version.from(request, orElse = Version4),
              params = Map("nino" -> nino),
              requestBody = Some(request.body)
            )
          )
          .withPlainJsonResult(CREATED)

      requestHandler.handleRequest()
    }

}
