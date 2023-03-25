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

import api.controllers.ControllerBaseSpec
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockListHistoricUkPropertyPeriodSummariesRequestParser
import v2.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListHistoricUkPropertyPeriodSummariesService, MockMtdIdLookupService}
import v2.models.domain.HistoricPropertyType
import api.models.errors._
import api.models.ResponseWrapper
import api.models.domain.Nino
import api.models.hateoas.HateoasWrapper
import v2.models.request.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesRawData, ListHistoricUkPropertyPeriodSummariesRequest}
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesHateoasData, ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListHistoricUkPropertyPeriodSummariesService
    with MockListHistoricUkPropertyPeriodSummariesRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val correlationId = "X-123"

  val hc: HeaderCarrier = HeaderCarrier()

  val controller = new ListHistoricUkPropertyPeriodSummariesController(
    authService = mockEnrolmentsAuthService,
    lookupService = mockMtdIdLookupService,
    parser = mockListHistoricUkPropertyPeriodSummariesRequestParser,
    service = mockService,
    hateoasFactory = mockHateoasFactory,
    cc = cc,
    idGenerator = mockIdGenerator
  )

  trait Test {
    MockMtdIdLookupService.lookup(nino) returns Future.successful(Right("test-mtd-id"))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId returns correlationId
  }

  private val rawData     = ListHistoricUkPropertyPeriodSummariesRawData(nino)
  private val requestData = ListHistoricUkPropertyPeriodSummariesRequest(Nino(nino))

  private val submissionPeriod = SubmissionPeriod("fromDate", "toDate")
  private val response         = ListHistoricUkPropertyPeriodSummariesResponse(Seq(submissionPeriod))

  "handleRequest" should {
    "return Ok" when {

      def success(handler: Action[AnyContent], propertyType: HistoricPropertyType): Unit =
        "the request is valid and processed successfully" in new Test {

          MockListHistoricUkPropertyPeriodSummariesRequestParser
            .parseRequest(rawData) returns Right(requestData)

          MockListHistoricUkPropertyPeriodSummariesService
            .listPeriodSummaries(requestData, propertyType) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

          val responseWithLinks: HateoasWrapper[ListHistoricUkPropertyPeriodSummariesResponse[HateoasWrapper[SubmissionPeriod]]] =
            HateoasWrapper(ListHistoricUkPropertyPeriodSummariesResponse(Seq(HateoasWrapper(submissionPeriod, testHateoasLinks))), testHateoasLinks)

          MockHateoasFactory
            .wrapList(response, ListHistoricUkPropertyPeriodSummariesHateoasData(nino, propertyType)) returns responseWithLinks

          val result: Future[Result] = handler(fakeRequest)

          contentAsJson(result) shouldBe Json.toJson(responseWithLinks)
          status(result) shouldBe OK
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }

      "FHL" when success(controller.handleFhlRequest(nino), HistoricPropertyType.Fhl)
      "Non-FHL" when success(controller.handleNonFhlRequest(nino), HistoricPropertyType.NonFhl)
    }

    "return an error as per spec" when {

      def parseErrors(handler: Action[AnyContent]): Unit =
        "parser errors occur" should {
          def parseError(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser" in new Test {

              MockListHistoricUkPropertyPeriodSummariesRequestParser
                .parseRequest(rawData) returns Left(ErrorWrapper(correlationId, error, None))

              val result: Future[Result] = handler(fakeRequest)

              contentAsJson(result) shouldBe Json.toJson(error)
              status(result) shouldBe expectedStatus
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }

          val input = Seq(
            (BadRequestError, BAD_REQUEST),
            (NinoFormatError, BAD_REQUEST)
          )

          input.foreach(args => (parseError _).tupled(args))
        }

      "FHL" when parseErrors(controller.handleFhlRequest(nino))
      "Non-FHL" when parseErrors(controller.handleNonFhlRequest(nino))

      def serviceErrors(handler: Action[AnyContent], propertyType: HistoricPropertyType): Unit = "service errors occur" should {
        def serviceError(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListHistoricUkPropertyPeriodSummariesRequestParser
              .parseRequest(rawData) returns Right(requestData)

            MockListHistoricUkPropertyPeriodSummariesService
              .listPeriodSummaries(requestData, propertyType) returns Future.successful(Left(ErrorWrapper(correlationId, mtdError)))

            val result: Future[Result] = handler(fakeRequest)

            contentAsJson(result) shouldBe Json.toJson(mtdError)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }

      "FHL" when serviceErrors(controller.handleFhlRequest(nino), HistoricPropertyType.Fhl)
      "Non-FHL" when serviceErrors(controller.handleNonFhlRequest(nino), HistoricPropertyType.NonFhl)
    }
  }
}
