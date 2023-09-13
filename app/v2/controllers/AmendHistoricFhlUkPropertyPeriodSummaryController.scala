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

package v2.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.models.audit.FlattenedGenericAuditDetail
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version2}
import utils.IdGenerator
import v2.controllers.validators.AmendHistoricFhlUkPeriodSummaryValidatorFactory
import v2.models.response.amendHistoricFhlUkPiePeriodSummary.AmendHistoricFhlUkPropertyPeriodSummaryHateoasData
import v2.services.AmendHistoricFhlUkPiePeriodSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendHistoricFhlUkPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                   val lookupService: MtdIdLookupService,
                                                                   validatorFactory: AmendHistoricFhlUkPeriodSummaryValidatorFactory,
                                                                   service: AmendHistoricFhlUkPiePeriodSummaryService,
                                                                   hateoasFactory: HateoasFactory,
                                                                   auditService: AuditService,
                                                                   cc: ControllerComponents,
                                                                   idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendHistoricFhlUkPropertyPeriodSummaryController",
      endpointName = "AmendHistoricFhlUkPropertyPeriodSummary"
    )

  def handleRequest(nino: String, periodId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request: UserRequest[JsValue] =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, periodId, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withAuditing(AuditHandler.custom(
            auditService,
            auditType = "AmendHistoricFhlPropertyIncomeExpensesPeriodSummary",
            transactionName = "amend-historic-fhl-property-income-expenses-period-summary",
            auditDetailCreator = FlattenedGenericAuditDetail.auditDetailCreator(
              Version.from(request, orElse = Version2),
              Map("nino" -> nino, "periodId" -> periodId)
            ),
            requestBody = Some(request.body),
            responseBodyMap = None => None
          ))
          .withHateoasResult(hateoasFactory)(AmendHistoricFhlUkPropertyPeriodSummaryHateoasData(nino, periodId))

      requestHandler.handleRequest()
    }

}
