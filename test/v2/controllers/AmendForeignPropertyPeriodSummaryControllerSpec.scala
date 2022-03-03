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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockAmendForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockAmendForeignPropertyPeriodSummaryService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method._
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea.{AmendForeignFhlEea, AmendForeignFhlEeaExpenses, ForeignFhlEeaIncome}
import v2.models.request.common.foreignPropertyEntry._
import v2.models.response.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyPeriodSummaryService
    with MockAmendForeignPropertyPeriodSummaryRequestParser
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val businessId    = "XAIS12345678910"
  private val taxYear       = "2022-23"
  private val submissionId  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyPeriodSummaryRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val requestBody: AmendForeignPropertyPeriodSummaryRequestBody =
    AmendForeignPropertyPeriodSummaryRequestBody(
      Some(
        AmendForeignFhlEea(
          Some(ForeignFhlEeaIncome(Some(5000.99))),
          Some(
            AmendForeignFhlEeaExpenses(
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              None
            ))
        )),
      Some(
        Seq(AmendForeignNonFhlPropertyEntry(
          "FRA",
          Some(
            ForeignNonFhlPropertyIncome(
              Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
              foreignTaxCreditRelief = false,
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99)
            )),
          Some(
            AmendForeignNonFhlPropertyExpenses(
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              None
            ))
        )))
    )

  val requestBodyWithConsolidatedExpense: AmendForeignPropertyPeriodSummaryRequestBody =
    AmendForeignPropertyPeriodSummaryRequestBody(
      Some(
        AmendForeignFhlEea(
          Some(ForeignFhlEeaIncome(Some(5000.99))),
          Some(
            AmendForeignFhlEeaExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              Some(5000.99)
            ))
        )),
      Some(
        Seq(AmendForeignNonFhlPropertyEntry(
          "FRA",
          Some(
            ForeignNonFhlPropertyIncome(
              Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
              foreignTaxCreditRelief = false,
              Some(5000.99),
              Some(5000.99),
              Some(5000.99),
              Some(5000.99)
            )),
          Some(
            AmendForeignNonFhlPropertyExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              Some(5000.99),
              Some(5000.99),
              None,
              Some(5000.99)
            ))
        )))
    )

  private val requestBodyJson = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """
      |{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val requestData = AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, submissionId, requestBody)
  private val rawData     = AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJson)

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |  "links": [
      |    {
      |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
      |      "method":"GET",
      |      "rel":"self"
      |    },
      |    {
      |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
      |      "method":"PUT",
      |      "rel":"amend-foreign-property-period-summary"
      |    },
      |    {
      |      "href":"/individuals/business/property/$nino/$businessId/period/$taxYear",
      |      "method":"GET",
      |      "rel":"list-property-period-summaries"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val testHateoasLinks =
    Seq(
      Link(href = s"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self"),
      Link(href = s"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
           method = PUT,
           rel = "amend-foreign-property-period-summary"),
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear", method = GET, rel = "list-property-period-summaries")
    )

  def event(requestBody: JsValue, auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "AmendForeignPropertyIncomeAndExpensesPeriodSummary",
      transactionName = "amend-foreign-property-income-and-expenses-period-summary",
      detail = GenericAuditDetail(
        versionNumber = "2.0",
        userType = "Individual",
        agentReferenceNumber = None,
        params = Json.obj("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "submissionId" -> submissionId, "request" -> requestBody),
        correlationId = correlationId,
        response = auditResponse
      )
    )

  "amend" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {

        MockAmendForeignPropertyRequestParser
          .parseRequest(AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJsonConsolidatedExpenses))
          .returns(Right(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] =
          controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequestWithBody(requestBodyJsonConsolidatedExpenses))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(requestBodyJsonConsolidatedExpenses, auditResponse)).once
      }
    }
    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {

        MockAmendForeignPropertyRequestParser
          .parseRequest(AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJson))
          .returns(Right(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequestWithBody(requestBodyJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignPropertyRequestParser
              .parseRequest(rawData.copy(body = requestBodyJson))
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] =
              controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
          }
        }

        val paths = Some(Seq("/path"))
        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError.copy(paths = paths), BAD_REQUEST),
          (ValueFormatError.copy(paths = paths), BAD_REQUEST),
          (RuleBothExpensesSuppliedError.copy(paths = paths), BAD_REQUEST),
          (RuleDuplicateCountryCodeError.copy(paths = paths), BAD_REQUEST),
          (CountryCodeFormatError.copy(paths = paths), BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = paths), BAD_REQUEST)
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

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleDuplicateCountryCodeError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
