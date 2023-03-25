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

import akka.http.scaladsl.model.headers.LinkParams.rel
import api.controllers.ControllerBaseSpec
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockRetrieveForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveForeignPropertyPeriodSummaryService}
import v2.models.domain.TaxYear
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.ResponseWrapper
import api.models.hateoas.{HateoasWrapper, Link}
import v2.models.request.retrieveForeignPropertyPeriodSummary._
import v2.models.response.retrieveForeignPropertyPeriodSummary._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyPeriodSummaryService
    with MockRetrieveForeignPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val businessId    = "XAIS12345678910"
  private val submissionId  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "X-123"
  private val taxYear       = "2022-23"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveForeignPropertyRequestParser,
      service = mockRetrieveForeignPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId)
  private val requestData = RetrieveForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId)

  private val testHateoasLink =
    Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

  private val responseBody = RetrieveForeignPropertyPeriodSummaryResponse(
    submittedOn = "",
    fromDate = "",
    toDate = "",
    foreignFhlEea = Some(
      ForeignFhlEea(
        income = Some(
          ForeignFhlEeaIncome(
            rentAmount = Some(3426.34)
          )),
        expenses = Some(ForeignFhlEeaExpenses(
          premisesRunningCosts = Some(1000.12),
          repairsAndMaintenance = Some(1000.12),
          financialCosts = Some(1000.12),
          professionalFees = Some(1000.12),
          costOfServices = Some(1000.12),
          travelCosts = Some(1000.12),
          other = Some(1000.12),
          consolidatedExpenses = None
        ))
      )),
    foreignNonFhlProperty = Some(
      Seq(
        ForeignNonFhlProperty(
          countryCode = "ZZZ",
          income = Some(ForeignNonFhlPropertyIncome(
            rentIncome = Some(ForeignNonFhlPropertyRentIncome(
              rentAmount = Some(1000.12)
            )),
            foreignTaxCreditRelief = true,
            premiumsOfLeaseGrant = Some(1000.12),
            otherPropertyIncome = Some(1000.12),
            foreignTaxPaidOrDeducted = Some(1000.12),
            specialWithholdingTaxOrUkTaxPaid = Some(1000.12)
          )),
          expenses = Some(ForeignNonFhlPropertyExpenses(
            premisesRunningCosts = Some(1000.12),
            repairsAndMaintenance = Some(1000.12),
            financialCosts = Some(1000.12),
            professionalFees = Some(1000.12),
            costOfServices = Some(1000.12),
            travelCosts = Some(1000.12),
            residentialFinancialCost = Some(1000.12),
            broughtFwdResidentialFinancialCost = Some(1000.12),
            other = Some(1000.12),
            consolidatedExpenses = None
          ))
        )
      ))
  )

  "Retrieve Foreign property period summary" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {

        MockRetrieveForeignPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return validation error as per spec" when {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockRetrieveForeignPropertyRequestParser
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
        (BusinessIdFormatError, BAD_REQUEST),
        (SubmissionIdFormatError, BAD_REQUEST),
        (RuleTaxYearNotSupportedError, BAD_REQUEST),
        (RuleTaxYearRangeInvalidError, BAD_REQUEST)
      )

      input.foreach(args => (errorsFromParserTester _).tupled(args))
    }

    "return service errors occur" when {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveForeignPropertyRequestParser
            .parse(rawData)
            .returns(Right(requestData))

          MockRetrieveForeignPropertyService
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
