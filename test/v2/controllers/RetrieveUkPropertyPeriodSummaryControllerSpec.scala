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
import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockRetrieveUkPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveUkPropertyPeriodSummaryService}
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors._
import api.hateoas.Method.GET
import api.hateoas.{HateoasWrapper, Link}
import api.models.ResponseWrapper
import v2.models.request.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryRawData, RetrieveUkPropertyPeriodSummaryRequest}
import v2.models.response.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with ResponseModelsFixture {

  private val nino          = "AA123456A"
  private val businessId    = "XAIS12345678910"
  private val submissionId  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "X-123"
  private val taxYear       = "2022-23"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveUkPropertyRequestParser,
      service = mockRetrieveUkPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveUkPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId)
  private val requestData = RetrieveUkPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId)

  private val testHateoasLink =
    Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

  private val responseBody = fullResponseModel

  "Retrieve UK property period summary" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {

        MockRetrieveUkPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return validation error as per spec" when {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockRetrieveUkPropertyRequestParser
            .parse(rawData)
            .returns(Left(ErrorWrapper(correlationId, error, None)))

          val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(error)
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

      val input = Seq(
        (BadRequestError, BAD_REQUEST),
        (NinoFormatError, BAD_REQUEST),
        (TaxYearFormatError, BAD_REQUEST),
        (RuleTaxYearNotSupportedError, BAD_REQUEST),
        (RuleTaxYearRangeInvalidError, BAD_REQUEST),
        (BusinessIdFormatError, BAD_REQUEST),
        (SubmissionIdFormatError, BAD_REQUEST)
      )

      input.foreach(args => (errorsFromParserTester _).tupled(args))
    }

    "return service errors occur" when {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveUkPropertyRequestParser
            .parse(rawData)
            .returns(Right(requestData))

          MockRetrieveUkPropertyService
            .retrieve(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (BusinessIdFormatError, BAD_REQUEST),
        (SubmissionIdFormatError, BAD_REQUEST),
        (TaxYearFormatError, BAD_REQUEST),
        (RuleTaxYearNotSupportedError, BAD_REQUEST),
        (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (InternalError, INTERNAL_SERVER_ERROR),
        (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }
}
