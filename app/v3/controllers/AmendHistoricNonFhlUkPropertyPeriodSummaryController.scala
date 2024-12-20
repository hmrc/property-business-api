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
import api.models.audit.FlattenedGenericAuditDetail
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.routing.Version
import utils.IdGenerator
import v3.controllers.validators.AmendHistoricNonFhlUkPeriodSummaryValidatorFactory
import v3.models.response.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData
import v3.services.AmendHistoricNonFhlUkPiePeriodSummaryService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AmendHistoricNonFhlUkPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                      val lookupService: MtdIdLookupService,
                                                                      validatorFactory: AmendHistoricNonFhlUkPeriodSummaryValidatorFactory,
                                                                      service: AmendHistoricNonFhlUkPiePeriodSummaryService,
                                                                      hateoasFactory: HateoasFactory,
                                                                      auditService: AuditService,
                                                                      cc: ControllerComponents,
                                                                      idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "amend-historic-non-fhluk-property-period-summary"

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "AmendHistoricNonFhlUkPropertyPeriodSummaryController",
    endpointName = "AmendHistoricNonFhlUkPropertyPeriodSummary")

  def handleRequest(nino: String, periodId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, periodId, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withAuditing(AuditHandler.custom(
            auditService,
            auditType = "AmendHistoricNonFhlPropertyIncomeExpensesPeriodSummary",
            transactionName = "amend-historic-non-fhl-property-income-expenses-period-summary",
            auditDetailCreator = FlattenedGenericAuditDetail.auditDetailCreator(
              Version(request),
              Map("nino" -> nino, "periodId" -> periodId)
            ),
            requestBody = Some(request.body),
            responseBodyMap = None => None
          ))
          .withHateoasResult(hateoasFactory)(AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData(nino, periodId))

      requestHandler.handleRequest()
    }

}
