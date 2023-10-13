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
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import api.routing.{Version, Version2}
import api.utils.IdGenerator
import v2.controllers.validators.CreateUkPropertyPeriodSummaryValidatorFactory
import v2.models.response.createUkPropertyPeriodSummary.CreateUkPropertyPeriodSummaryHateoasData
import v2.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateUkPropertyPeriodSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                         val lookupService: MtdIdLookupService,
                                                         validatorFactory: CreateUkPropertyPeriodSummaryValidatorFactory,
                                                         service: CreateUkPropertyPeriodSummaryService,
                                                         auditService: AuditService,
                                                         hateoasFactory: HateoasFactory,
                                                         cc: ControllerComponents,
                                                         idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateUkPropertyController", endpointName = "Create a Uk Property Income & Expenditure Period Summary")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createUkProperty)
        .withAuditing(
          AuditHandler(
            auditService,
            "CreateUKPropertyIncomeAndExpensesPeriodSummary",
            "create-uk-property-income-and-expenses-period-summary",
            Version.from(request, orElse = Version2),
            Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
            Some(request.body),
            includeResponse = true
          )
        )
        .withHateoasResultFrom(hateoasFactory)(
          (_, resp) => CreateUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, resp.submissionId),
          CREATED)

      requestHandler.handleRequest()

    }

}
