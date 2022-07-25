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
import v2.mocks.requestParsers.MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{
  MockAuditService,
  MockEnrolmentsAuthService,
  MockMtdIdLookupService,
  MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
}
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{ HateoasWrapper, Link }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission._
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.{
  AnnualAdjustments,
  AnnualAllowances,
  RentARoom,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2020-21"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser,
      service = mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, taxYear)
  private val requestData = RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink =
    Link(href = s"individuals/business/property/uk/non-furnished-holiday-lettings/$nino/$taxYear", method = GET, rel = "self")

  private val annualAdjustments = AnnualAdjustments(
    lossBroughtForward = Some(BigDecimal("200.00")),
    balancingCharge = Some(BigDecimal("300.00")),
    privateUseAdjustment = Some(BigDecimal("400.00")),
    businessPremisesRenovationAllowanceBalancingCharges = Some(BigDecimal("80.02")),
    nonResidentLandlord = true,
    rentARoom = Option(RentARoom(jointlyLet = true))
  )

  private val annualAllowances = AnnualAllowances(
    annualInvestmentAllowance = Some(BigDecimal("200.00")),
    otherCapitalAllowance = Some(BigDecimal("300.00")),
    zeroEmissionGoodsVehicleAllowance = Some(BigDecimal("400.00")),
    businessPremisesRenovationAllowance = Some(BigDecimal("200.00")),
    costOfReplacingDomesticGoods = Some(BigDecimal("200.00")),
    propertyIncomeAllowance = Some(BigDecimal("30.02"))
  )

  val responseBody: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
    RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(Some(annualAdjustments), Some(annualAllowances))

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return an error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

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
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
