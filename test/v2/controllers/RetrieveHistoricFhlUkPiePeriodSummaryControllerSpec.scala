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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{ MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveHistoricFhlUkPiePeriodSummaryService }
import v2.models.domain.{ Nino, PeriodId }
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{ HateoasWrapper, Link }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary._
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricFhlUkPiePeriodSummaryService
    with MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"

  private val from     = "2017-04-06"
  private val to       = "2017-07-04"
  private val periodId = s"${from}_$to"

  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser,
      service = mockRetrieveHistoricFhlUkPiePeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveHistoricFhlUkPiePeriodSummaryRawData(nino, periodId)
  private val requestData = RetrieveHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))

  private val mockHateoasLink =
    Link(href = s"individuals/business/property/uk/period/furnished-holiday-lettings/$nino/$periodId", method = GET, rel = "self")

  val periodIncome: PeriodIncome = PeriodIncome(Some(5000.99), Some(5000.99), Some(RentARoomIncome(Some(5000.99))))

  val periodExpenses: PeriodExpenses = PeriodExpenses(Some(5000.99),
                                                      Some(5000.99),
                                                      Some(5000.99),
                                                      Some(5000.99),
                                                      Some(5000.99),
                                                      Some(5000.99),
                                                      None,
                                                      Some(5000.99),
                                                      Some(RentARoomExpenses(Some(5000.99))))

  val responseBody: RetrieveHistoricFhlUkPiePeriodSummaryResponse = RetrieveHistoricFhlUkPiePeriodSummaryResponse(
    fromDate = from,
    toDate = to,
    Some(periodIncome),
    Some(periodExpenses)
  )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveHistoricFhlUkPropertyRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricFhlUkPiePeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveHistoricFhlUkPiePeriodSummaryHateoasData(nino, periodId))
          .returns(HateoasWrapper(responseBody, Seq(mockHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return an error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveHistoricFhlUkPropertyRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (PeriodIdFormatError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (NotFoundError, NOT_FOUND),
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveHistoricFhlUkPropertyRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockRetrieveHistoricFhlUkPiePeriodSummaryService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (PeriodIdFormatError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (NotFoundError, NOT_FOUND),
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
