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

import common.models.audit.FlattenedGenericAuditDetail
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.hateoas.HateoasFactory
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v3.controllers.validators.CreateHistoricFhlUkPiePeriodSummaryValidatorFactory
import v3.models.response.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryHateoasData
import v3.services.CreateHistoricFhlUkPiePeriodSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               validatorFactory: CreateHistoricFhlUkPiePeriodSummaryValidatorFactory,
                                                               service: CreateHistoricFhlUkPiePeriodSummaryService,
                                                               auditService: AuditService,
                                                               hateoasFactory: HateoasFactory,
                                                               cc: ControllerComponents,
                                                               idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create-historic-fhluk-pie-period-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateHistoricFhlUkPiePeriodSummaryController",
      endpointName = "createHistoricFhlUkPropertyIncomeAndExpensesPeriodSummary"
    )

  def handleRequest(nino: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.createPeriodSummary)
          .withAuditing(
            AuditHandler.custom(
              auditService,
              "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
              "create-historic-fhl-property-income-expenses-period-summary",
              auditDetailCreator = FlattenedGenericAuditDetail.auditDetailCreator(
                Version(request),
                Map("nino" -> nino)
              ),
              requestBody = Some(request.body),
              responseBodyMap = None => None
            )
          )
          .withHateoasResultFrom(hateoasFactory)((_, response) => CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino, response.periodId), CREATED)

      requestHandler.handleRequest()
    }

}
