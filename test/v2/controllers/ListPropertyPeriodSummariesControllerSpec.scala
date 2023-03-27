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
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockListPropertyPeriodSummariesRequestParser
import v2.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListPropertyPeriodSummariesService, MockMtdIdLookupService}
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import v2.models.request.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesRawData, ListPropertyPeriodSummariesRequest}
import v2.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesHateoasData, ListPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListPropertyPeriodSummariesService
    with MockListPropertyPeriodSummariesRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val businessId    = "XAIS12345678910"
  private val taxYear       = "2020-21"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockListPropertyPeriodSummariesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = ListPropertyPeriodSummariesRawData(nino, businessId, taxYear)
  private val requestData = ListPropertyPeriodSummariesRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear))

  private val testHateoasLink = Link("/someLink", GET, "some-relation")

  val response: ListPropertyPeriodSummariesResponse =
    ListPropertyPeriodSummariesResponse(Seq(SubmissionPeriod("someId", "fromDate", "toDate")))

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockListPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockListPropertyPeriodSummariesService
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val responseWithLinks: HateoasWrapper[ListPropertyPeriodSummariesResponse] = HateoasWrapper(response, Seq(testHateoasLink))
        MockHateoasFactory
          .wrap(response, ListPropertyPeriodSummariesHateoasData(nino, businessId, taxYear))
          .returns(responseWithLinks)

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

        contentAsJson(result) shouldBe Json.toJson(responseWithLinks)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }

    "return an error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListPropertyPeriodSummariesRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

            contentAsJson(result) shouldBe Json.toJson(error)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListPropertyPeriodSummariesRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockListPropertyPeriodSummariesService
              .listPeriodSummaries(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

            contentAsJson(result) shouldBe Json.toJson(mtdError)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
