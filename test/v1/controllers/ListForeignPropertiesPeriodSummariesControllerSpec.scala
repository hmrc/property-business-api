/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockListForeignPropertiesPeriodSummariesRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListForeignPropertiesPeriodSummariesService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesRawData, ListForeignPropertiesPeriodSummariesRequest}
import v1.models.response.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesHateoasData, ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod}
import v1.models.hateoas.RelType.SELF

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListForeignPropertiesPeriodSummariesControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListForeignPropertiesPeriodSummariesService
    with MockListForeignPropertiesPeriodSummariesRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new ListForeignPropertiesPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockListForeignPropertiesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val nino = "AA123456A"
  val businessId = "XAIS12345678910"
  val fromDate = "2020-06-01"
  val toDate = "2020-08-31"
  private val correlationId = "X-123"

  val request = ListForeignPropertiesPeriodSummariesRequest(Nino(nino), businessId, fromDate, toDate)

  val responseModel1 = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")
  val responseModel2 = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")

  val response = ListForeignPropertiesPeriodSummariesResponse(Seq(
    responseModel1,
    responseModel2
  ))

  private val testHateoasLink = Link(href = s"Individuals/business/property/$nino/$businessId/period", method = GET, rel = "self")

  private val responseModel1TestHateoasLink = Link(
    href = s"Individuals/business/property/$nino/$businessId/period/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    method = GET,
    rel = SELF
  )

  private val responseModel2TestHateoasLink = Link(
    href = s"Individuals/business/property/$nino/$businessId/period/4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
    method = GET,
    rel = SELF
  )

  private val hateoasResponse = ListForeignPropertiesPeriodSummariesResponse(
    Seq(
      HateoasWrapper(responseModel1,
        Seq(
          responseModel1TestHateoasLink
        )
      ),
      HateoasWrapper(responseModel2,
        Seq(
          responseModel2TestHateoasLink
        )
      )
    )
  )

  val serviceResponse = ListForeignPropertiesPeriodSummariesResponse(Seq(
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
  ))

  private val rawData = ListForeignPropertiesPeriodSummariesRawData(nino, businessId, Some(fromDate), Some(toDate))
  private val requestData = ListForeignPropertiesPeriodSummariesRequest(Nino(nino), businessId, fromDate, toDate)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockListForeignPropertiesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockListForeignPropertiesService
          .listForeignProperties(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, serviceResponse))))

        MockHateoasFactory
          .wrapList(serviceResponse, ListForeignPropertiesPeriodSummariesHateoasData(nino, businessId))
          .returns(HateoasWrapper(hateoasResponse, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, Some(fromDate), Some(toDate))(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListForeignPropertiesRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, Some(fromDate), Some(toDate))(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListForeignPropertiesRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockListForeignPropertiesService
              .listForeignProperties(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, Some(fromDate), Some(toDate))(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}