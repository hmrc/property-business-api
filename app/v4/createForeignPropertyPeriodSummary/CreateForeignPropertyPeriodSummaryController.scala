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

package v4.createForeignPropertyPeriodSummary

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
class CreateForeignPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                              val lookupService: MtdIdLookupService,
                                                              validatorFactory: CreateForeignPropertyPeriodSummaryValidatorFactory,
                                                              service: CreateForeignPropertyPeriodSummaryService,
                                                              auditService: AuditService,
                                                              cc: ControllerComponents,
                                                              idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-foreign-property-period-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateForeignPropertyPeriodSummaryController",
      endpointName = "Create a Foreign Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.createForeignProperty)
          .withAuditing(
            AuditHandler(
              auditService,
              "CreateForeignPropertyIncomeAndExpensesPeriodSummary",
              "create-foreign-property-income-and-expenses-period-summary",
              Version.from(request, orElse = Version3),
              Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
              Some(request.body)
            )
          )
          .withPlainJsonResult(CREATED)

      requestHandler.handleRequest()
    }

}
