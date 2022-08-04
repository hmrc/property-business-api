/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HttpClient
import utils.{IdGenerator, Logging}
import v2.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import v2.controllers.requestParsers.validators.Validator
import v2.controllers.requestParsers.RequestParser
import v2.hateoas.HateoasFactory
import v2.models.errors._
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryRawData, CreateHistoricFhlUkPiePeriodSummaryRequest, CreateHistoricFhlUkPiePeriodSummaryRequestBody}
import v2.models.response.createHistoricFhlUkPiePeriodSummary.{CreateHistoricFhlUkPiePeriodSummaryHateoasData, CreateHistoricFhlUkPiePeriodSummaryResponse}
import v2.services.{CreateUkPropertyPeriodSummaryService, EnrolmentsAuthService, MtdIdLookupService}
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                              val lookupService: MtdIdLookupService,
                                                              parser: CreateHistoricFhlUkPiePeriodSummaryParser,
                                                              service: CreateUkPropertyPeriodSummaryService, //TODO: Update service
                                                              hateoasFactory: HateoasFactory,
                                                              cc: ControllerComponents,
                                                              idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateHistoricFhlUkPiePeriodSummaryController",
      endpointName = "Create a Historic UK Furnished Holiday Letting Property Income & Expenses Period Summary")

  def handleRequest(nino: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(
        message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with correlationId : $correlationId")

      val rawData = CreateHistoricFhlUkPiePeriodSummaryRawData(nino, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createUkProperty(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap((serviceResponse.responseData,
                CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino,
                  s"${parsedRequest.body.toDate}_${parsedRequest.body.toDate}", serviceResponse.responseData.submissionId) //TODO: transactionreference
              )).asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
          val response = Json.toJson((vendorResponse))
          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case (BadRequestError | NinoFormatError | RuleBothExpensesSuppliedError | ValueFormatError |
        RuleIncorrectOrEmptyBodyError | FromDateFormatError |ToDateFormatError |
        RuleToDateBeforeFromDateError | RuleDuplicateSubmissionError | RuleMisalignedPeriodError |
        RuleOverlappingPeriodError | RuleNotContiguousPeriodError | RuleTaxYearNotSupportedError)
      => BadRequest(Json.toJson(errorWrapper))
      case UnauthorisedError => Unauthorized(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }
}
//TODO: delete and replace placeholder code (below)
class CreateHistoricFhlUkPiePeriodSummaryParser  @Inject()()
  extends RequestParser[CreateHistoricFhlUkPiePeriodSummaryRawData, CreateHistoricFhlUkPiePeriodSummaryRequest] {

  override protected def requestFor(data: CreateHistoricFhlUkPiePeriodSummaryRawData): CreateHistoricFhlUkPiePeriodSummaryRequest = ???

  def parseRequest(data: CreateHistoricFhlUkPiePeriodSummaryRawData):  CreateHistoricFhlUkPiePeriodSummaryRequest = ???

  override val validator: Validator[CreateHistoricFhlUkPiePeriodSummaryRawData] = ???
}

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[CreateHistoricFhlUkPiePeriodSummaryRawData] {
  override def validate(data: CreateHistoricFhlUkPiePeriodSummaryRawData): List[MtdError] = ???
}

@Singleton
class CreateHistoricFhlUkPiePeriodSummaryService @Inject()(connector: CreateHistoricFhlUkPiePeriodSummaryConnector)
  extends DownstreamResponseMappingSupport
    with Logging {

  def createPeriodSummary(request: CreateHistoricFhlUkPiePeriodSummaryRequest)(): CreateHistoricFhlUkPiePeriodSummaryResponse = ???

}


@Singleton
class CreateHistoricFhlUkPiePeriodSummaryConnector @Inject()(val http: HttpClient, val appConfig: AppConfig)
  extends BaseDownstreamConnector {
}



