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
import v1.mocks.requestParsers.MockAmendForeignPropertyPeriodSummaryRequestParser
import v1.mocks.services.{MockAmendForeignPropertyPeriodSummaryService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.GET
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignPropertyPeriodSummary.{AmendForeignPropertyPeriodSummaryRawData, AmendForeignPropertyPeriodSummaryRequest, AmendForeignPropertyPeriodSummaryRequestBody}
import v1.models.request.common.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenditure, ForeignFhlEeaIncome}
import v1.models.request.common.foreignPropertyEntry.{ForeignPropertyEntry, ForeignPropertyExpenditure, ForeignPropertyIncome, ForeignPropertyRentIncome}
import v1.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyPeriodSummaryService
    with MockAmendForeignPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "X-123"

  private val testHateoasLink = Link(href = s"Individuals/business/property/$nino/$businessId/period", method = GET, rel = "self")

  private val requestJson = Json.parse(
    """|{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 567.83,
       |      "taxDeducted": 4321.92
       |      },
       |    "expenditure": {
       |      "premisesRunningCosts": 4567.98,
       |      "repairsAndMaintenance": 98765.67,
       |      "financialCosts": 4566.95,
       |      "professionalFees": 23.65,
       |      "costsOfServices": 4567.77,
       |      "travelCosts": 456.77,
       |      "other": 567.67
       |    }
       |  },
       |  "foreignProperty": [{
       |      "countryCode": "zzz",
       |      "income": {
       |        "rentIncome": {
       |          "rentAmount": 34456.30,
       |          "taxDeducted": 6334.34
       |        },
       |        "foreignTaxCreditRelief": true,
       |        "premiumOfLeaseGrant": 2543.43,
       |        "otherPropertyIncome": 54325.30,
       |        "foreignTaxTakenOff": 6543.01,
       |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
       |      },
       |      "expenditure": {
       |        "premisesRunningCosts": 5635.43,
       |        "repairsAndMaintenance": 3456.65,
       |        "financialCosts": 34532.21,
       |        "professionalFees": 32465.32,
       |        "costsOfServices": 2567.21,
       |        "travelCosts": 2345.76,
       |        "residentialFinancialCost": 21235.22,
       |        "broughtFwdResidentialFinancialCost": 12556.00,
       |        "other": 2425.11
       |      }
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  private val foreignFhlEea: ForeignFhlEea = ForeignFhlEea(
    income = ForeignFhlEeaIncome(rentAmount = 567.83),
    expenditure = Some(ForeignFhlEeaExpenditure(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(4566.95),
      professionalFees = Some(23.65),
      costsOfServices = Some(4567.77),
      travelCosts = Some(456.77),
      other = Some(567.67),
      consolidatedExpenses = None
    ))
  )

  private val foreignProperty: ForeignPropertyEntry = ForeignPropertyEntry(
    countryCode = "zzz",
    income = ForeignPropertyIncome(
      rentIncome = ForeignPropertyRentIncome(rentAmount = 34456.30),
      foreignTaxCreditRelief = true,
      premiumOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxTakenOff = Some(6543.01),
      specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
    ),
    expenditure = Some(ForeignPropertyExpenditure(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costsOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = None
    ))
  )

  val requestBody: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignProperty = Some(Seq(foreignProperty))
  )

  private val rawData = AmendForeignPropertyPeriodSummaryRawData(nino, businessId, submissionId, requestJson)
  private val requestData = AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, submissionId, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendForeignPropertyRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, submissionId))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, submissionId)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignPropertyRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, submissionId)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (CountryCodeFormatError.copy(paths = Some(Seq(
            "foreignProperty/0/countryCode"))), BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "foreignFhlEea/income/rentAmount",
            "foreignFhlEea/expenditure/repairsAndMaintenance",
            "foreignFhlEea/expenditure/professionalFees",
            "foreignFhlEea/expenditure/other",
            "foreignProperty/income/rentIncome/rentAmount",
            "foreignProperty/expenditure/professionalFees",
            "foreignProperty/expenditure/other"))), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = Some(Seq(
            "foreignProperty/0/countryCode"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignPropertyRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendForeignPropertyService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, submissionId)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (SubmissionIdNotFoundError, NOT_FOUND),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}