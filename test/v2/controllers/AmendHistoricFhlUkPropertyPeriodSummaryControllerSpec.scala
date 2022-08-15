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

import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockAmendHistoricFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{ MockAmendHistoricFhlUkPropertyPeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService }
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.HateoasWrapper
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendHistoricFhlUkPiePeriodSummary._
import v2.models.response.amendHistoricFhlUkPiePeriodSummary.AmendHistoricFhlUkPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val periodId      = "somePeriodId"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendHistoricFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendHistoricFhlUkPropertyPeriodSummaryRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  // Doesn't matter what there are: parser is mocked
  private val requestBodyJson = JsObject.empty
  private val requestBody     = AmendHistoricFhlUkPiePeriodSummaryRequestBody(None, None)

  private val rawData = AmendHistoricFhlUkPiePeriodSummaryRawData(nino, periodId, requestBodyJson)
  private val request = AmendHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), periodId, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendHistoricFhlUkPropertyPeriodSummaryRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockAmendHistoricFhlUkPropertyPeriodSummaryService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendHistoricFhlUkPropertyPeriodSummaryHateoasData(nino, periodId))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe testHateoasLinksJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendHistoricFhlUkPropertyPeriodSummaryRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (withPath(RuleBothExpensesSuppliedError), BAD_REQUEST),
          (withPath(ValueFormatError), BAD_REQUEST),
          (withPath(RuleIncorrectOrEmptyBodyError), BAD_REQUEST),
          (FormatPeriodIdError, BAD_REQUEST),
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendHistoricFhlUkPropertyPeriodSummaryRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockAmendHistoricFhlUkPropertyPeriodSummaryService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, periodId)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (FormatPeriodIdError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
