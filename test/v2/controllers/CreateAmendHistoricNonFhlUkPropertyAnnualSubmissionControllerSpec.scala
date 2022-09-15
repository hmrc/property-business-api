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

import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionFixture
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{ MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService, MockEnrolmentsAuthService, MockMtdIdLookupService }
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{ HateoasWrapper, Link }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData,
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest
}
import v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData,
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionFixture {

  private val nino          = "AA123456A"
  private val taxYear       = "2022-23"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockCreateAmendHistoricService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLink = Link(href = s"/individuals/business/property/annual/$nino/annual/$taxYear", method = GET, rel = "self")

  val hateoasResponse: JsValue = Json.parse(s"""
       |{
       |   "links": [
       |      {
       |         "href": "/individuals/business/property/annual/$nino/annual/$taxYear",
       |         "method": "GET",
       |         "rel": "self"
       |      }
       |   ]
       |}
    """.stripMargin)

  private val rawData = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear, validMtdJson)
  private val request = CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))))

        MockHateoasFactory
          .wrap(CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None),
                CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))
          .returns(HateoasWrapper(CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleHistoricTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (ValueFormatError.copy(
             paths = Some(
               List(
                 "/annualAdjustments/lossBroughtForward",
                 "/annualAdjustments/balancingCharge",
                 "annualAllowances/annualInvestmentAllowance"
               ))
           ),
           BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parseRequest(rawData)
              .returns(Right(request))

            MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
              .amend(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequestWithBody(validMtdJson))

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
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
