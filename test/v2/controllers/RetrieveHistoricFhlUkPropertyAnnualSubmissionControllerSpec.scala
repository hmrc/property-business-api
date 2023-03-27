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
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.requestParsers.MockRetrieveHistoricFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveHistoricFhlUkPropertyAnnualSubmissionService}
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.{RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData, RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest}
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricFhlUkPropertyAnnualSubmissionService
    with MockRetrieveHistoricFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val mtdTaxYear    = "2020-21"
  private val taxYear       = TaxYear.fromMtd(mtdTaxYear)
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockRetrieveHistoricFhlUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData(nino, mtdTaxYear)
  private val requestData = RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), taxYear)

  val annualAdjustments: AnnualAdjustments = AnnualAdjustments(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("200.11")),
    Option(BigDecimal("105.11")),
    true,
    Option(BigDecimal("100.11")),
    false,
    Option(RentARoom(true))
  )

  val annualAllowances: AnnualAllowances = AnnualAllowances(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("300.11")),
    Option(BigDecimal("405.11")),
    Option(BigDecimal("550.11"))
  )

  private val mockHateoasLink =
    Link(href = s"individuals/business/property/uk/annual/furnished-holiday-lettings/$nino/$mtdTaxYear", method = GET, rel = "self")

  val responseBody: RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse = RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(
    Some(annualAdjustments),
    Some(annualAllowances)
  )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveHistoricFhlUkPropertyRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricFhlUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino, mtdTaxYear))
          .returns(HateoasWrapper(responseBody, Seq(mockHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, mtdTaxYear)(fakeRequest)
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

            val result: Future[Result] = controller.handleRequest(nino, mtdTaxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveHistoricFhlUkPropertyRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockRetrieveHistoricFhlUkPropertyService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, mtdTaxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
            (RuleIncorrectGovTestScenarioError, BAD_REQUEST)

        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
