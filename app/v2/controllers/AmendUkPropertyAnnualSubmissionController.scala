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
import utils.IdGenerator
import v2.controllers.requestParsers.AmendUkPropertyAnnualSubmissionRequestParser
import v2.models.request.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionRawData
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionHateoasData
import v2.models.response.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionResponse.AmendUkPropertyLinksFactory
import v2.services.AmendUkPropertyAnnualSubmissionService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendUkPropertyAnnualSubmissionController @Inject() (val authService: EnrolmentsAuthService,
                                                           val lookupService: MtdIdLookupService,
                                                           parser: AmendUkPropertyAnnualSubmissionRequestParser,
                                                           service: AmendUkPropertyAnnualSubmissionService,
                                                           auditService: AuditService,
                                                           hateoasFactory: HateoasFactory,
                                                           cc: ControllerComponents,
                                                           idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendUkPropertyAnnualSubmissionController", endpointName = "AmendUkPropertyAnnualSubmission")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = AmendUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.amendUkPropertyAnnualSubmission)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendUKPropertyAnnualSubmission",
          transactionName = "create-amend-uk-property-annual-submission",
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "request" -> request.body.as[String]),
          requestBody = None,
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(AmendUkPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))

      requestHandler.handleRequest(rawData)

    }

}
